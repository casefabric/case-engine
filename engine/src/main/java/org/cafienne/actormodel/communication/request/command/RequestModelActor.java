package org.cafienne.actormodel.communication.request.command;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.communication.CaseSystemCommunicationCommand;
import org.cafienne.actormodel.communication.request.response.ActorRequestDeliveryReceipt;
import org.cafienne.actormodel.communication.request.state.RemoteActorState;
import org.cafienne.actormodel.exception.InvalidCommandException;
import org.cafienne.actormodel.message.command.BootstrapMessage;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.actormodel.message.response.ModelResponse;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;

@Manifest
public class RequestModelActor extends CaseSystemCommunicationCommand implements BootstrapMessage {
    public final boolean debugMode;
    public final ActorMetadata source;

    public RequestModelActor(ModelCommand command, RemoteActorState<?> state) {
        super(state.target, command);
        this.debugMode = state.actor.debugMode();
        this.source = state.source;
    }

    public RequestModelActor(ValueMap json) {
        super(json);
        this.debugMode = json.readBoolean(Fields.debugMode);
        this.source = json.readMetadata(Fields.source);
    }

    @Override
    public String getCommandDescription() {
        return "Requested[" + command.getDescription() +"]";
    }

    @Override
    public void validate(ModelActor modelActor) throws InvalidCommandException {
        // No validation required, that is done inside the wrapped command, when the corresponding ActorRequest event is executed
        if (command.isBootstrapMessage()) {
            // Need to do this immediately to have more logging available in the debug event
            this.actor.setDebugMode(debugMode);
        }
    }

    @Override
    public void process(ModelActor actor) {
        actor.getIncomingRequestState().handleIncomingRequest(this);
    }

    @Override
    public ModelResponse getResponse() {
        return new ActorRequestDeliveryReceipt(source, this.command);
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.writeActorCommand(generator);
        writeField(generator, Fields.source, source);
        writeField(generator, Fields.debugMode, debugMode);
    }

    @Override
    public boolean isBootstrapMessage() {
        return command.isBootstrapMessage();
    }

    @Override
    public String tenant() {
        return command.asBootstrapMessage().tenant();
    }
}
