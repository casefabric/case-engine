package org.cafienne.actormodel.communication;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.ActorType;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.identity.UserIdentity;
import org.cafienne.actormodel.message.command.BaseModelCommand;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;

public abstract class CaseSystemCommunicationCommand extends BaseModelCommand<ModelActor, UserIdentity> implements CaseSystemCommunicationMessage {
    public final ModelCommand command;

    protected CaseSystemCommunicationCommand(ModelCommand command) {
        super(command.getUser(), command.actorId());
        this.command = command;
    }

    protected CaseSystemCommunicationCommand(ValueMap json) {
        super(json);
        this.command = json.readManifestField(Fields.command);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "[" + command.getDescription() + "]";
    }

    @Override
    public ActorType actorType() {
        return command.actorType();
    }

    @Override
    public String getCorrelationId() {
        return command.getCorrelationId();
    }

    protected void writeActorCommand(JsonGenerator generator) throws IOException {
        super.writeModelCommand(generator);
        writeManifestField(generator, Fields.command, command);
    }
}
