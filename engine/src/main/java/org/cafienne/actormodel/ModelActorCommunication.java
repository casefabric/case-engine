package org.cafienne.actormodel;

import org.cafienne.actormodel.communication.reply.state.IncomingRequestState;
import org.cafienne.actormodel.communication.request.state.RemoteActorState;

import java.util.HashMap;
import java.util.Map;

public class ModelActorCommunication {
    private final ModelActor actor;
    private final IncomingRequestState incomingRequests;
    private final Map<String, RemoteActorState<?>> remoteActors = new HashMap<>();

    ModelActorCommunication(ModelActor actor) {
        this.actor = actor;
        this.incomingRequests = new IncomingRequestState(actor);
    }

    void register(RemoteActorState<?> remoteActorState) {
        this.remoteActors.put(remoteActorState.target.actorId(), remoteActorState);
    }

    RemoteActorState<?> getRemoteActorState(ActorMetadata remoteActorMetadata) {
        RemoteActorState<?> state = remoteActors.get(remoteActorMetadata.actorId());
        if (state == null) {
            System.out.println(actor.metadata +": Cannot find state for path " + remoteActorMetadata.path());
        }
        return state;
    }

    IncomingRequestState getIncomingRequestState() {
        return incomingRequests;
    }

    void recoveryCompleted() {
        incomingRequests.recoveryCompleted();
        remoteActors.forEach((string, state) -> state.recoveryCompleted());
    }
}
