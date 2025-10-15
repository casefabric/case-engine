/*
 * Copyright (C) 2014  Batav B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cafienne.actormodel;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.persistence.journal.Tagged;
import org.cafienne.actormodel.communication.reply.command.RunActorRequest;
import org.cafienne.actormodel.communication.reply.event.ActorRequestFailed;
import org.cafienne.actormodel.communication.request.response.ActorRequestFailure;
import org.cafienne.actormodel.debug.DebugInfoAppender;
import org.cafienne.actormodel.exception.AuthorizationException;
import org.cafienne.actormodel.exception.CommandException;
import org.cafienne.actormodel.exception.InvalidCommandException;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.actormodel.message.event.CommitEvent;
import org.cafienne.actormodel.message.event.DebugEvent;
import org.cafienne.actormodel.message.event.EngineVersionChanged;
import org.cafienne.actormodel.message.event.ModelEvent;
import org.cafienne.actormodel.message.response.*;
import org.cafienne.infrastructure.EngineVersion;
import org.cafienne.infrastructure.enginedeveloper.EngineDeveloperConsole;
import org.cafienne.system.health.HealthMonitor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ModelActorTransaction captures all state changing events upon handling an {@link ModelCommand}
 * It also handles failures and sending responses to complete the lifecycle of the message.
 */
public class ModelActorTransaction extends UserMessageTransaction<ModelCommand> {
    private final ActorRef sender;
    private final static int avgNumEvents = 30;
    private final List<ModelEvent> events = new ArrayList<>(avgNumEvents);
    private ModelResponse response = null;
    private final TransactionLogger logger;
    private final ModelActorMonitor monitor;

    ModelActorTransaction(ModelActor actor, Reception reception, ModelActorMonitor monitor, ModelCommand command) {
        super(actor, reception, command);
        this.monitor = monitor;
        // Tell the actor monitor we're busy
        monitor.setBusy();
        this.sender = actor.sender();
        this.logger = new TransactionLogger(this, actor);
    }

    void perform() {
        actor.addDebugInfo(() -> "---------- User " + message.getUser().id() + " in " + actor + " receives message " + message.getDescription(), message.rawJson());
        if (reception.canPass(message)) {
            // First check the engine version, potentially leading to an extra event.
            this.checkEngineVersion();

            try {
                // First, simple, validation
                message.validateCommand(actor);
                // Then, do actual work of processing in the command itself.
                message.processCommand(actor);
                setResponse(message.getResponse());
            } catch (AuthorizationException e) {
                reportFailure(e, new SecurityFailure(message, e), "");
            } catch (InvalidCommandException e) {
                reportFailure(message, e, "===== Command was invalid ======");
            } catch (CommandException e) {
                reportFailure(message, e, "---------- User " + message.getUser().id() + " in " + this.actor + " failed to complete command " + message + "\nwith exception");
            } catch (Throwable e) {
                reportFailure(e, new ActorChokedFailure(message, e), "---------- Engine choked during validation of command with type " + message.getClass().getSimpleName() + " from user " + message.getUser().id() + " in " + this.actor + "\nwith exception");
            }
            commit();
        }
        // Tell the actor monitor we're free again
        monitor.setFree();
    }

    private void checkEngineVersion() {
        // First check whether the engine version has changed or not; this may lead to an EngineVersionChanged event
        EngineVersion actorVersion = actor.getEngineVersion();
        EngineVersion currentEngineVersion = actor.caseSystem.version();
        if (actorVersion != null && currentEngineVersion.differs(actor.getEngineVersion())) {
            actor.getLogger().info(actor + " changed engine version from\n" + actor.getEngineVersion() + " to\n" + currentEngineVersion);
            addEvent(new EngineVersionChanged(actor, currentEngineVersion));
        }
    }

    /**
     * Add an event and update the actor state for it.
     */
    @Override
    void addEvent(ModelEvent event) {
        events.add(event);
        addDebugInfo(getLogger(), () -> "Updating actor state for new event " + event.getDescription());
        event.updateActorState(actor);
    }

    private Logger getLogger() {
        return actor.getLogger();
    }

    public boolean hasState() {
        return !hasFailures() && hasStatefulEvents();
    }

    /**
     * Store events in persistence and optionally reply a response to the sender of the incoming message.
     */
    void commit() {
        // Handling the incoming message can result in 3 different scenarios that are dealt with below:
        // 1. The message resulted in an exception that needs to be returned to the client; Possibly the case must be restarted.
        // 2. The message resulted in state changes, so the new events need to be persisted, and after persistence the response is sent back to the client.
        // 3. The message did not result in state changes (e.g., when fetching discretionary items), and the response can be sent straight away
        if (hasFailures()) {
            // Inform the sender about the failure and then store the debug event if any
            replyAndPersistDebugEvent(response);

            // If we have created events (other than debug events) from the failure, then we are in inconsistent state and need to restart the actor.
            if (hasStatefulEvents()) {
                Throwable exception = ((CommandFailure) response).internalException();
                actor.addDebugInfo(() -> {
                    StringBuilder msg = new StringBuilder("\n------------------------ ABORTING PERSISTENCE OF " + events.size() + " EVENTS IN " + actor);
                    events.forEach(e -> msg.append("\n\t").append(e.getDescription()));
                    return msg + "\n";
                }, exception);
                actor.failedWithInvalidState(message, exception);
            }
        } else {
            // If there are only debug events, first respond and then persist the events (for performance).
            // Otherwise, only send a response upon successful persisting the events.
            if (hasStatefulEvents()) {
                if (!(events.getLast() instanceof CommitEvent)) {
                    actor.addCommitEvent(message);
                }
                persistEventsAndThenReply(response);
            } else {
                actor.notModified(message);
                replyAndPersistDebugEvent(response);
            }
        }
    }

    private void replyAndPersistDebugEvent(ModelResponse response) {
        // Inform the sender about the failure
        actor.reply(response, sender);
        // In case of failure we still want to store the debug event. Actually, mostly we need this in case of failure (what else are we debugging for)
        if (this.logger.hasDebugEvent()) {
            actor.persistAsync(addTags(logger.getDebugEvent()), e -> {});
        }
    }

    private Tagged addTags(ModelEvent event) {
        return new Tagged(event, event.tags());
    }

    private void persistEventsAndThenReply(ModelResponse response) {
        if (getLogger().isDebugEnabled() || EngineDeveloperConsole.enabled()) {
            StringBuilder msg = new StringBuilder("\n------------------------ PERSISTING " + events.size() + " EVENTS IN " + actor);
            events.forEach(e -> msg.append("\n\t").append(e));
            getLogger().debug("{}\n", msg);
            EngineDeveloperConsole.debugIndentedConsoleLogging(msg + "\n");
        }
        // Include the debug event if any.
        if (logger.hasDebugEvent()) {
            events.addFirst(logger.getDebugEvent());
        }

        // Apply tagging to the events
        final List<Tagged> taggedEvents = events.stream().map(this::addTags).collect(Collectors.toList());
        // When the last event is persisted, we can send a reply. Keep track of that last event here, so that we need not go through the list each time.
        final Tagged lastTaggedEvent = taggedEvents.getLast();

        actor.persistAll(taggedEvents, persistedEvent -> {
            HealthMonitor.writeJournal().isOK();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(actor + " - persisted event [" + actor.lastSequenceNr() + "] of type " + persistedEvent.payload().getClass().getName());
            }
            if (persistedEvent == lastTaggedEvent) {
                actor.reply(response, sender);
                events.forEach(event -> event.afterPersist(actor));
            }
        });
    }

    /**
     * When back office encounters command handling failures, they can report it here.
     * Stateful events are not stored, DebugEvent would still get stored.
     */
    void reportFailure(ModelCommand command, Throwable exception, String msg) {
        reportFailure(exception, new CommandFailure(command, exception), msg);
    }

    /**
     * When back office encounters command handling failures, they can report it here.
     * Stateful events are not stored, DebugEvent would still get stored.
     */
    void reportFailure(Throwable exception, CommandFailure failure, String msg) {
        actor.addDebugInfo(() -> "", exception, msg);
        if (this.message instanceof RunActorRequest actorRequest) {
            this.addEvent(new ActorRequestFailed(actorRequest, exception));
            this.response = new ActorRequestFailure(actorRequest.command, exception);
        } else {
            this.response = failure;
        }
    }

    /**
     * To be invoked upon successful command handling.
     */
    void setResponse(ModelResponse response) {
        this.response = response;
    }

    /**
     * Hook to optionally tell the sender about persistence failures
     */
    @Override
    public void handlePersistFailure(Throwable cause, Object event, long seqNr) {
        actor.reply(new EngineChokedFailure(message, new Exception("Handling the request resulted in a system failure. Check the server logs for more information.")), sender);
    }

    private boolean hasFailures() {
        return response instanceof CommandFailure;
    }

    /**
     * Simplistic
     */
    private boolean hasStatefulEvents() {
        return !events.isEmpty() && !(events.size() == 1 && events.getFirst() instanceof DebugEvent);
    }

    /**
     * Add debug info to the ModelActor if debug is enabled.
     * If the actor runs in debug mode (or if slf4j has debug enabled for this logger),
     * then the appender.debugInfo(...) method will be invoked to store a string in the log.
     *
     * @param logger         The slf4j logger instance to check whether debug logging is enabled
     * @param appender       A functional interface returning "an" object, holding the main info to be logged.
     *                       Note: the interface is only invoked if logging is enabled. This appender typically
     *                       returns a String that is only created upon demand (in order to speed up a bit)
     * @param additionalInfo Additional objects to be logged. Typically, pointers to existing objects.
     */
    @Override
    public void addDebugInfo(Logger logger, DebugInfoAppender appender, Object... additionalInfo) {
        this.logger.addDebugInfo(logger, appender, additionalInfo);
    }
}
