package org.cafienne.actormodel;

import org.cafienne.actormodel.communication.receiver.state.IncomingRequestState;
import org.cafienne.actormodel.communication.sender.state.RemoteActorState;

import java.util.HashMap;
import java.util.Map;

public class ModelActorCommunication {
    private final IncomingRequestState incomingRequests;
    private final Map<String, RemoteActorState<?>> remoteActors = new HashMap<>();

    ModelActorCommunication(ModelActor actor) {
        this.incomingRequests = new IncomingRequestState(actor);
    }

    void register(RemoteActorState<?> remoteActorState) {
        this.remoteActors.put(remoteActorState.receiver.actorId(), remoteActorState);
    }

    /**
     * Note: this method can return a null object. Handling that situation is left to the responsibility of the code that invokes this method.
     */
    RemoteActorState<?> getRemoteActorState(ActorMetadata remoteActorMetadata) {
        return remoteActors.get(remoteActorMetadata.actorId());
    }

    IncomingRequestState getIncomingRequestState() {
        return incomingRequests;
    }

    void recoveryCompleted() {
        incomingRequests.recoveryCompleted();
        remoteActors.forEach((string, state) -> state.recoveryCompleted());
    }
}
