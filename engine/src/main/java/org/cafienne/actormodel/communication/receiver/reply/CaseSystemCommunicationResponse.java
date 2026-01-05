package org.cafienne.actormodel.communication.receiver.reply;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.communication.CaseSystemCommunicationCommand;
import org.cafienne.actormodel.communication.sender.state.RemoteActorState;
import org.cafienne.actormodel.exception.InvalidCommandException;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.actormodel.message.response.ActorLastModified;
import org.cafienne.actormodel.message.response.ModelResponse;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;
import java.time.Instant;

/**
 * Note: the "response" is actually done by sending them as a command back from the "incoming" side of the house
 */
public abstract class CaseSystemCommunicationResponse extends CaseSystemCommunicationCommand implements ModelResponse {
    private Instant lastModified;

    protected CaseSystemCommunicationResponse(ModelActor sender, ActorMetadata receiver, ModelCommand command) {
        super(sender, receiver, command);
        this.lastModified = command.getActor() != null ? command.getActor().getLastModified() : null;
    }

    protected CaseSystemCommunicationResponse(ValueMap json) {
        super(json);
        this.lastModified = json.readInstant(Fields.lastModified);
    }

    @Override
    public final void validate(ModelActor actor) throws InvalidCommandException {
        // Must have state
    }

    @Override
    public final void process(ModelActor actor) {
//        System.out.println(actor + ": fetching state for response from this.target " + target + " with this.target() " + this.target() +" and this.command.target(): " + command.target());
        RemoteActorState<?> state = actor.getRemoteActorState(this.command.target());
        if (state == null) {
            // We have not seen this happen, so it is weird if ... But logging it to the error log anyway
            actor.addDebugInfo(() -> "ERROR: " + actor + " cannot find a state for handling " + this.getDescription() + " from actor " + command.actorId() + " in " + this.target);
        } else {
            process(state);
        }
    }

    protected abstract void process(RemoteActorState<?> state);

    @Override
    public final ModelResponse getResponse() {
        // Sending responses might have cyclic side effects, can't send one therefore.
        return null;
    }

    @Override
    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public ActorLastModified lastModifiedContent() {
        return new ActorLastModified(actorId, lastModified);
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.writeActorCommand(generator);
        writeField(generator, Fields.lastModified, this.lastModified);
    }
}
