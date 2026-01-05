package org.cafienne.actormodel.communication.sender.command;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.communication.CaseSystemCommunicationCommand;
import org.cafienne.actormodel.communication.receiver.reply.ActorRequestDeliveryReceipt;
import org.cafienne.actormodel.communication.sender.state.RemoteActorState;
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

    public RequestModelActor(ModelCommand command, RemoteActorState<?> state) {
        super(state.actor, state.receiver, command);
        this.debugMode = state.actor.debugMode();
    }

    public RequestModelActor(ValueMap json) {
        super(json);
        this.debugMode = json.readBoolean(Fields.debugMode);
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
        return new ActorRequestDeliveryReceipt(this.actor, sender, this.command);
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.writeActorCommand(generator);
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
