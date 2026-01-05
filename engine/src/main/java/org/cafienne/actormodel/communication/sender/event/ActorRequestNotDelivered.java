package org.cafienne.actormodel.communication.sender.event;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.communication.CaseSystemCommitEvent;
import org.cafienne.actormodel.communication.sender.command.ActorRequestDeliveryFailure;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;

@Manifest
public class ActorRequestNotDelivered extends ModelActorReplyEvent implements CaseSystemCommitEvent {
    public final String errorMessage;

    public ActorRequestNotDelivered(ModelActor actor, ActorRequestDeliveryFailure failure) {
        super(actor, failure.getCorrelationId(), failure.receiver);
        this.errorMessage = failure.errorMessage;
    }

    public ActorRequestNotDelivered(ValueMap json) {
        super(json);
        this.errorMessage = json.readString(Fields.failure);
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.writeActorRequestEvent(generator);
        writeField(generator, Fields.failure, errorMessage);
    }
}
