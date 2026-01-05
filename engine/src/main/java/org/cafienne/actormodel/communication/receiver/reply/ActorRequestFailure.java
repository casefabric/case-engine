package org.cafienne.actormodel.communication.receiver.reply;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.communication.sender.state.RemoteActorState;
import org.cafienne.actormodel.exception.SerializedException;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.util.json.Value;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;

@Manifest
public class ActorRequestFailure extends CaseSystemCommunicationResponse {
    private final Throwable exception;
    public final SerializedException serializedException;
    public final ValueMap exceptionAsJSON;

    public ActorRequestFailure(ModelActor sender, ActorMetadata receiver, ModelCommand command, Throwable failure) {
        super(sender, receiver, command);
        this.exception = failure;
        this.exceptionAsJSON = Value.convertThrowable(failure);
        this.serializedException = new SerializedException(failure);
    }

    public ActorRequestFailure(ValueMap json) {
        super(json);
        this.exception = null;
        this.exceptionAsJSON = json.readMap(Fields.exception);
        this.serializedException = new SerializedException(exceptionAsJSON);
    }

    @Override
    public boolean actorChanged() {
        return false;
    }

    @Override
    protected void process(RemoteActorState<?> state) {
        state.registerFailure(this);
    }

    @Override
    public String getCommandDescription() {
        return "ActorRequestHandlingFailure[" + command.getDescription() +"]";
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.writeActorCommand(generator);
        writeField(generator, Fields.exception, exceptionAsJSON);
    }
}