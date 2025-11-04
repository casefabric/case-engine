package org.cafienne.actormodel.communication.receiver.event;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.communication.CaseSystemCommunicationEvent;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;

public abstract class ModelActorRequestEvent extends CaseSystemCommunicationEvent {
    public final ActorMetadata sender;

    protected ModelActorRequestEvent(ModelCommand command, ActorMetadata sender) {
        super(command.getActor(), command.getCorrelationId());
        this.sender = sender;
    }

    protected ModelActorRequestEvent(ValueMap json) {
        super(json);
        this.sender = readSender(json, Fields.sourceActorId);
    }

    @Override
    public void updateState(ModelActor actor) {
        actor.getIncomingRequestState().updateState(this);
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        writeIncomingRequestEvent(generator);
    }

    public void writeIncomingRequestEvent(JsonGenerator generator) throws IOException {
        super.writeActorRequestEvent(generator);
        writeSender(generator, sender);
    }
}
