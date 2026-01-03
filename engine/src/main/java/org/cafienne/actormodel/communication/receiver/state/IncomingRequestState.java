package org.cafienne.actormodel.communication.receiver.state;

import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.communication.receiver.event.ActorRequestStored;
import org.cafienne.actormodel.communication.receiver.event.ModelActorRequestEvent;
import org.cafienne.actormodel.communication.sender.command.RequestModelActor;

import java.util.HashMap;
import java.util.Map;

public class IncomingRequestState {
    private final Map<String, IncomingRequest> requests = new HashMap<>();
    final ModelActor actor;

    public IncomingRequestState(ModelActor actor) {
        this.actor = actor;
    }

    public void handleIncomingRequest(RequestModelActor request) {
//        System.out.println("\n" + actor +"   RECEIVED REQUEST " + request.getCommandDescription() +" with id " + request.getMessageId());
        IncomingRequest incoming = requests.get(request.getCorrelationId());
        if (incoming == null) {
            incoming = new IncomingRequest(this);
            requests.put(request.getCorrelationId(), incoming);
            this.actor.addEvent(new ActorRequestStored(request));
        } else {
//            System.out.println("Gettging same request again, ignoring it.");
//            actor.getLogger().warn("Received same request again");
        }
    }

    public void updateState(ModelActorRequestEvent event) {
        IncomingRequest incoming = requests.computeIfAbsent(event.getCorrelationId(), k -> new IncomingRequest(this));
        incoming.updateState(event);
    }

    public void recoveryCompleted() {
        requests.values().forEach(IncomingRequest::recoveryCompleted);
    }
}
