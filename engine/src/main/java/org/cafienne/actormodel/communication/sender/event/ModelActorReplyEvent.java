package org.cafienne.actormodel.communication.sender.event;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.communication.CaseSystemCommunicationEvent;
import org.cafienne.actormodel.communication.sender.state.RemoteActorState;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;

public abstract class ModelActorReplyEvent extends CaseSystemCommunicationEvent {
    public final ActorMetadata receiver;

    protected ModelActorReplyEvent(ModelActor actor, String messageId, ActorMetadata receiver) {
        super(actor, messageId);
        this.receiver = receiver;
    }

    protected ModelActorReplyEvent(ValueMap json) {
        super(json);
        this.receiver = readReceiver(json);
    }

    @Override
    public void updateState(ModelActor actor) {
        RemoteActorState<?> state = actor.getRemoteActorState(this.receiver);
        if (state == null) {
            // We have not seen this happen, so it is weird if ... But logging it to the error log anyway
            actor.addDebugInfo(() -> "ERROR: " + actor.metadata + " cannot find a state for handling " + this.getDescription() + " from remote actor " + receiver);
        } else {
            state.updateState(this);
        }
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        writeOutgoingRequestEvent(generator);
    }

    public void writeOutgoingRequestEvent(JsonGenerator generator) throws IOException {
        super.writeActorRequestEvent(generator);
        writeReceiver(generator, receiver);
    }
}
