package org.cafienne.actormodel.communication;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ActorType;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.identity.UserIdentity;
import org.cafienne.actormodel.message.command.BaseModelCommand;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;

public abstract class CaseSystemCommunicationCommand extends BaseModelCommand<ModelActor, UserIdentity> implements CaseSystemCommunicationMessage {
    private ModelCommand deserializedModelCommand;
    public final ActorMetadata target;
    public final ModelCommand command;

    protected CaseSystemCommunicationCommand(ActorMetadata target, ModelCommand command) {
        super(command.getUser(), command.actorId());
        this.target = target;
        this.command = this.deserializedModelCommand = command;
    }

    protected CaseSystemCommunicationCommand(ValueMap json) {
        super(json);
        this.target = json.readMetadata(Fields.target);
        this.command = readCommand(json);
    }

    private ModelCommand readCommand(ValueMap json) {
        if (this.deserializedModelCommand == null) {
            this.deserializedModelCommand = json.readManifestField(Fields.command);
        }
        return this.deserializedModelCommand;
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "[" + command.getDescription() + "]";
    }

    @Override
    public ActorType actorType() {
        if (command == null) {
            // This code is typically read during json deserialization of the UserIdentity in the super class
            // In that case the command is still null. Therefore, we deserialize it
            //  and only then read the actor type from it.
            // (Deserializing is optimized through a private field in order to do it only once)
            if (this.json != null) {
                return readCommand(json).actorType();
            } else {
                // Let's just make it not crash and see what happens ...
                return ActorType.ModelActor;
            }
        } else {
            return command.actorType();
        }
    }

    @Override
    public String getCorrelationId() {
        return command.getCorrelationId();
    }

    protected void writeActorCommand(JsonGenerator generator) throws IOException {
        super.writeModelCommand(generator);
        writeField(generator, Fields.target, target);
        writeManifestField(generator, Fields.command, command);
    }
}
