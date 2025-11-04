package org.cafienne.actormodel.communication.sender.command;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.communication.CaseSystemCommunicationCommand;
import org.cafienne.actormodel.communication.sender.event.ActorRequestNotDelivered;
import org.cafienne.actormodel.exception.InvalidCommandException;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;

@Manifest
public class ActorRequestDeliveryFailure extends CaseSystemCommunicationCommand {
    public final String errorMessage;
    public final ActorMetadata receiver;

    public ActorRequestDeliveryFailure(ModelActor sender, ActorMetadata receiver, ModelCommand command, String errorMessage) {
        super(sender, sender.metadata, command);
        this.errorMessage = errorMessage;
        this.receiver = receiver;
    }

    public ActorRequestDeliveryFailure(ValueMap json) {
        super(json);
        this.errorMessage = json.readString(Fields.failure);
        this.receiver = json.readMetadata(Fields.receiver);
    }

    @Override
    public void validate(ModelActor actor) throws InvalidCommandException {
        // Must have state
    }

    @Override
    public void process(ModelActor actor) {
        actor.addEvent(new ActorRequestNotDelivered(actor, this));
    }

    @Override
    public String getCommandDescription() {
        return "ActorRequestDeliveryFailure[" + command.getDescription() +"]";
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.writeActorCommand(generator);
        writeField(generator, Fields.receiver, receiver);
        writeField(generator, Fields.failure, errorMessage);
    }
}