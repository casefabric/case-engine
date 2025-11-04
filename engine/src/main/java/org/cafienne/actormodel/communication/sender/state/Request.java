package org.cafienne.actormodel.communication.sender.state;

import org.cafienne.actormodel.communication.sender.command.RequestModelActor;
import org.cafienne.actormodel.communication.sender.event.ActorRequestCreated;
import org.cafienne.actormodel.communication.sender.event.ActorRequestDelivered;
import org.cafienne.actormodel.communication.sender.event.ActorRequestNotDelivered;
import org.cafienne.actormodel.communication.sender.event.ModelActorReplyEvent;
import org.cafienne.actormodel.message.command.ModelCommand;

public class Request {
    private final RemoteActorState<?> state;
    private final RequestTracker tracker;
    private ActorRequestCreated creationEvent;
    private ActorRequestDelivered deliveryEvent;

    Request(RemoteActorState<?> state) {
        this.state = state;
        this.tracker = new RequestTracker(this, state.actor.caseSystem);
    }

    void updateState(ModelActorReplyEvent event) {
        switch (event) {
            case ActorRequestCreated arc -> created(arc);
            case ActorRequestDelivered ard -> delivered(ard);
            case ActorRequestNotDelivered nd -> notDelivered(nd);
            default -> {
                // Note: currently there are only 2 events that extend the ModelActorReplyEvent, so we can throw.
                throw new RuntimeException("BOOM on a missing handler for " + event.getClass().getName());
            }
        }
    }

    public ModelCommand getCommand() {
        return creationEvent.command;
    }

    public void created(ActorRequestCreated event) {
        this.creationEvent = event;
        if (state.actor.recoveryFinished()) {
//            System.err.println("FAILING HERE");
            send();
        }
    }

    public void delivered(ActorRequestDelivered event) {
//        System.out.println(this +" is delivered upon count " + tracker.count());
        this.deliveryEvent = event;
        tracker.stop();
    }

    public void notDelivered(ActorRequestNotDelivered event) {
        state.handleNotDelivered(event);
    }

    public void send() {
        //        System.out.println(this + ": request is not yet completed, sending it to " + state.targetActorId);
        this.state.actor.caseSystem.engine().inform(new RequestModelActor(creationEvent.command, state), state.actor.self());
        tracker.start();
    }

    void failed(String reason) {
        state.requestDeliveryFailed(this, reason);
    }

    @Override
    public String toString() {
        return creationEvent.command.getCommandDescription() + " from " + state.actor + " to " + state.receiver;
    }

    final void recoveryCompleted() {
        if (creationEvent != null && deliveryEvent == null) {
            state.actor.addDebugInfo(() -> state.actor + ": recovery completed, trying to send incompleted request of type " + creationEvent.command.getCommandDescription() + " to " + state.receiver);
            send();
        }
    }
}
