package org.cafienne.actormodel.communication.receiver.reply;

import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.communication.sender.state.RemoteActorState;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.util.json.ValueMap;

@Manifest
public class ActorRequestDeliveryReceipt extends CaseSystemCommunicationResponse {
    public ActorRequestDeliveryReceipt(ModelActor sender, ActorMetadata receiver, ModelCommand command) {
        super(sender, receiver, command);
    }

    public ActorRequestDeliveryReceipt(ValueMap json) {
        super(json);
    }

    @Override
    public boolean actorChanged() {
        return true;
    }

    @Override
    public String getCommandDescription() {
        return "Delivered[" + command.getDescription() + "]";
    }

    @Override
    protected void process(RemoteActorState<?> state) {
        state.registerDelivery(this);
    }
}
