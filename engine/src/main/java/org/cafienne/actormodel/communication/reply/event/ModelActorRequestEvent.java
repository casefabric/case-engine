package org.cafienne.actormodel.communication.reply.event;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.communication.CaseSystemCommunicationEvent;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;

public abstract class ModelActorRequestEvent extends CaseSystemCommunicationEvent {
    public final ActorMetadata source;

    protected ModelActorRequestEvent(ModelCommand command, ActorMetadata source) {
        super(command.getActor(), command.getCorrelationId());
        this.source = source;
    }

    protected ModelActorRequestEvent(ValueMap json) {
        super(json);
        this.source = json.readMetadata(Fields.source);
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
        writeField(generator, Fields.source, source);
    }
}
