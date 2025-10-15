package org.cafienne.actormodel;

import org.cafienne.actormodel.debug.DebugInfoAppender;
import org.cafienne.actormodel.identity.UserIdentity;
import org.cafienne.actormodel.message.UserMessage;
import org.cafienne.actormodel.message.event.ModelEvent;
import org.slf4j.Logger;

public abstract class MessageTransaction<M> {
    protected final M message;
    protected final ModelActor actor;
    protected final Reception reception;

    protected MessageTransaction(ModelActor actor, Reception reception, M message) {
        this.actor = actor;
        this.reception = reception;
        this.message = message;
        this.actor.setCurrentTransaction(this);
    }

    public abstract String getCorrelationId();

    public abstract UserIdentity getUser();

    abstract void perform();

    abstract void addEvent(ModelEvent event);

    /**
     * Add debug info to the ModelActor if debug is enabled.
     * If the actor runs in debug mode (or if slf4j has debug enabled for this logger),
     * then the appender's debugInfo method will be invoked to store a string in the log.
     *
     * @param logger         The slf4j logger instance to check whether debug logging is enabled
     * @param appender       A functional interface returning "an" object, holding the main info to be logged.
     *                       Note: the interface is only invoked if logging is enabled. This appender typically
     *                       returns a String that is only created upon demand (in order to speed up a bit)
     * @param additionalInfo Additional objects to be logged. Typically, pointers to existing objects.
     */
    void addDebugInfo(Logger logger, DebugInfoAppender appender, Object... additionalInfo) {
        // by default no log information is printed
    }

    void handlePersistFailure(Throwable cause, Object event, long seqNr) {
        // defaults to not doing anything
    }

    public boolean hasState() {
        return false;
    }
}

abstract class UserMessageTransaction<UM extends UserMessage> extends MessageTransaction<UM> {
    protected UserMessageTransaction(ModelActor actor, Reception reception, UM message) {
        super(actor, reception, message);
    }

    @Override
    public String getCorrelationId() {
        return message.getCorrelationId();
    }

    @Override
    public UserIdentity getUser() {
        return message.getUser();
    }
}
