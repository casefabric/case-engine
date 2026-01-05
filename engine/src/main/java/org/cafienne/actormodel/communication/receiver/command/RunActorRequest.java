package org.cafienne.actormodel.communication.receiver.command;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.communication.CaseSystemCommunicationCommand;
import org.cafienne.actormodel.communication.receiver.event.ActorRequestExecuted;
import org.cafienne.actormodel.communication.receiver.event.ActorRequestStored;
import org.cafienne.actormodel.exception.InvalidCommandException;
import org.cafienne.actormodel.message.response.ModelResponse;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;

/**
 * ActorRequests are sent between ModelActors. When a request is received by an actor,
 * it is stored as an ActorRequestReceived event, which is also a CommitEvent,
 * as in the event itself did not really make a functional change to the ModelActor.
 * This only happens when the ModelActor starts to handle the command that is stored inside the event.
 * This is done by telling actor.self() about the event - which is done only after the event was persisted.
 */
@Manifest
public class RunActorRequest extends CaseSystemCommunicationCommand {
    public RunActorRequest(ModelActor sender, ActorMetadata receiver, ActorRequestStored event) {
        super(sender, receiver, event.request.command);
    }

    public RunActorRequest(ValueMap json) {
        super(json);
    }

    @Override
    public void setActor(ModelActor actor) {
        // Setting the actor must also be done on the command we carry
        super.setActor(actor);
        command.setActor(actor);
    }

    @Override
    public void validate(ModelActor modelActor) throws InvalidCommandException {
        command.validateCommand(actor);
    }

    @Override
    public void process(ModelActor modelActor) {
        command.processCommand(actor);
        actor.addEvent(new ActorRequestExecuted(command, sender));
    }

    @Override
    public String getCommandDescription() {
        return "Run[" + command.getDescription() + "]";
    }

    @Override
    public ModelResponse getResponse() {
//        System.out.println(this.getDescription() + ": No need to return a response from command handling");
//        return new ActorRequestExecuted(request, request.command().getResponse());
        return null;
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.writeActorCommand(generator);
    }
}
