package org.cafienne.actormodel.communication.reply.event;

import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.util.json.ValueMap;

@Manifest
public class ActorRequestExecuted extends ModelActorRequestEvent {
    public ActorRequestExecuted(ModelCommand command, ActorMetadata source) {
        super(command, source);
    }

    public ActorRequestExecuted(ValueMap json) {
        super(json);
    }
}
