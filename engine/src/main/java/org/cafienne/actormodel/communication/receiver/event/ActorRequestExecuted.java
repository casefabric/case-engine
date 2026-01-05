package org.cafienne.actormodel.communication.receiver.event;

import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.util.json.ValueMap;

@Manifest
public class ActorRequestExecuted extends ModelActorRequestEvent {
    public ActorRequestExecuted(ModelCommand command, ActorMetadata sender) {
        super(command, sender);
    }

    public ActorRequestExecuted(ValueMap json) {
        super(json);
    }
}
