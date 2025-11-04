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
import org.cafienne.actormodel.communication.CaseSystemCommunicationMessage;
import org.cafienne.actormodel.communication.sender.command.RequestModelActor;
import org.cafienne.actormodel.exception.InvalidCommandException;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.actormodel.message.command.TerminateModelActor;
import org.cafienne.actormodel.message.event.ModelEvent;
import org.cafienne.actormodel.message.response.*;
import org.cafienne.infrastructure.serialization.DeserializationFailure;
import org.cafienne.service.storage.actormodel.message.StorageEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * All actor system incoming traffic (either during recovery or upon receiving incoming messages)
 * is passed through the Reception.
 * The reception knows whether the ModelActor is capable of handling the incoming traffic,
 * and when applicable creates a {@link ModelActorTransaction} to handle the message.
 */
public class Reception {
    private final ModelActor actor;
    private final ModelActorMonitor monitor;
    private boolean bootstrapPending = true;
    private boolean isBroken = false;
    private String recoveryFailureInformation = "";
    private boolean isInStorageProcess = false;
    private ActorType actorType;
    private List<ModelActorShutdownHook> hooks = new ArrayList<>();

    Reception(ModelActor actor) {
        this.actor = actor;
        this.monitor = new ModelActorMonitor(actor);
        if (actor.getLogger().isDebugEnabled()) {
            actor.getLogger().debug("Opening recovery of {}", actor);
        }
//        System.out.println("\n\n============ STARTING RECOVERY IN " + actor);
    }

    void handleRecovery(Object message) {
        if (isBroken()) {
            // Something has gone wrong with earlier recovery messages, no need to do further processing.
            return;
        }
        handleMessage(message);
    }

    private void terminateActor() {
        ActorRef replyTo = actor.sender();
        hooks.add(() -> replyTo.tell(new ActorTerminated(actor.getId()), actor.self()));
        actor.takeABreak("Upon request");
    }

    void handleMessage(Object message) {
        switch (message) {
            case TerminateModelActor tma -> terminateActor();
            case ModelEvent event -> new RecoveryTransaction(actor, this, event).perform();
            case ModelCommand command -> new ModelActorTransaction(actor, this, monitor, command).perform();
            case null, default -> new SystemMessageTransaction(actor, this, message).perform();
        }
    }

    boolean canPass(ModelCommand visitor) {
        // Incoming visitors of type model command need the actor for processing and responding.
        visitor.setActor(actor);

        if (isBroken()) {
            return informAboutRecoveryFailure(visitor);
        }

        if (!bootstrapPending && visitor.isBootstrapMessage() && !(visitor instanceof RequestModelActor)) {
            // Cannot run e.g. StartCase two times. Also, we should not reveal it already exists.
            handleAlreadyCreated(visitor);
            return false;
        }

        if (bootstrapPending) {
            if (visitor.isBootstrapMessage()) {
                actor.handleBootstrapMessage(visitor.asBootstrapMessage());
            } else {
                // Cannot run e.g. CompleteHumanTask if the Case is not yet created.
                //  Pretty weird if we get to this stage, as the Routes should not let it come here ...
                fail(visitor, "Expected bootstrap command in " + actor + " instead of " + visitor.getDescription());
                return false;
            }
        }

        if (visitor instanceof CaseSystemCommunicationMessage) {
            return true;
        }

        if (!actor.supportsCommand(visitor)) {
            fail(visitor, actor + " does not support commands of type " + visitor.getDescription());
            return false;
        }

        return true;
    }

    void unlock() {
        bootstrapPending = false;
    }

    private void hideFrontDoor(String msg) {
        isBroken = true;
        recoveryFailureInformation = msg;
    }

    private boolean isBroken() {
        return isBroken;
    }

    private boolean informAboutRecoveryFailure(ModelCommand msg) {
        actor.getLogger().warn("Aborting recovery of {} upon request of type {} from user {}. {}", actor, msg.getClass().getSimpleName(), msg.getUser().id(), recoveryFailureInformation);
        if (msg.isBootstrapMessage()) {
            // Trying to do e.g. StartCase in e.g. a TenantActor
            handleAlreadyCreated(msg);
        } else if (isInStorageProcess) {
            actor.reply(new ActorInStorage(msg, actorType), actor.sender());
        } else {
            String error = actor + " cannot handle message '" + msg.getClass().getSimpleName() + "' because it has not recovered properly. Check the server logs for more details.";
            actor.reply(new ActorChokedFailure(msg, new InvalidCommandException(error)), actor.sender());
        }
        actor.takeABreak("Removing ModelActor[" + actor.getId() + "] because of recovery failure upon unexpected incoming message of type " + msg.getClass().getSimpleName());
        return false;
    }

    private void handleAlreadyCreated(ModelCommand msg) {
        actor.reply(new ActorExistsFailure(msg, new IllegalArgumentException("Failure while handling message " + msg.getClass().getSimpleName() + ". Check the server logs for more details")), actor.sender());
    }

    private void fail(ModelCommand command, String errorMessage) {
        actor.reply(new CommandFailure(command, new InvalidCommandException(errorMessage)), actor.sender());
    }

    void reportDeserializationFailure(DeserializationFailure failure) {
        hideFrontDoor("" + failure);
        actor.getLogger().error("%s encountered a system error in deserializing a message".formatted(actor), failure);
        // These exceptions mean basically a bug while developing new commands/events/responses.
        // Therefore also log to console.
        failure.exception.printStackTrace(System.out);
    }

    void reportInvalidRecoveryEvent(ModelEvent event) {
        if (event.isBootstrapMessage()) {
            // Wrong type of ModelActor. Probably someone tries to re-use the same actor id for another type of ModelActor.
            hideFrontDoor("Recovery event " + event.getClass().getSimpleName() + " requires an actor of type " + event.actorType().actorClass.getSimpleName());
        } else if (event instanceof StorageEvent storageEvent) {
            // Someone is archiving or deleting this actor
            hideFrontDoor("Actor is in storage processing");
            isInStorageProcess = true;
            actorType = storageEvent.metadata().actorType();
        } else {
            // Pretty weird if it happens ...
            hideFrontDoor("Received unexpected recovery event of type " + event.getClass().getName());
        }
    }

    void reportStateUpdateFailure(Throwable throwable) {
        actor.getLogger().error("Unexpected error during recovery of " + actor, throwable);
        hideFrontDoor("Updating actor state failed.");
    }

    void open() {
        actor.informRecoveryCompletion();
    }

    void close() {
        monitor.cancelTimer(); // Clear in mem scheduler to stop the actor after idle time
        hooks.forEach(ModelActorShutdownHook::afterShutdown);
    }
}

@FunctionalInterface
interface ModelActorShutdownHook {
    void afterShutdown();
}
