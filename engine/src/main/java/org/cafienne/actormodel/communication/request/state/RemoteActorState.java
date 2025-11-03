package org.cafienne.actormodel.communication.request.state;

import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ActorType;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.communication.request.event.ActorRequestCreated;
import org.cafienne.actormodel.communication.request.event.ActorRequestDelivered;
import org.cafienne.actormodel.communication.request.event.ModelActorReplyEvent;
import org.cafienne.actormodel.communication.request.response.ActorRequestDeliveryReceipt;
import org.cafienne.actormodel.communication.request.response.ActorRequestFailure;
import org.cafienne.actormodel.message.command.ModelCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the state of interacting with the remote actor as perceived within the local actor
 *
 * @param <LocalActor> A typesafe version of the local actor, not of the remote one (!).
 */
public abstract class RemoteActorState<LocalActor extends ModelActor> {
    public final LocalActor actor;
    public final String targetActorId;
    public final ActorMetadata target;
    public final ActorMetadata source;
    private final Map<String, Request> requests = new HashMap<>();

    protected RemoteActorState(LocalActor actor, ActorMetadata target) {
        this.actor = actor;
        this.target = target;
        this.targetActorId = target.actorId();
        this.source = actor.metadata();
        this.actor.register(this);
    }

    public void sendRequest(ModelCommand command) {
        actor.addEvent(new ActorRequestCreated(this, command));
    }

    public final void registerDelivery(ActorRequestDeliveryReceipt receipt) {
        actor.addEvent(new ActorRequestDelivered(this, receipt));
        handleReceipt(receipt);
    }

    public final void registerFailure(ActorRequestFailure failure) {
        handleFailure(failure);
    }

    /**
     * Hook that can be invoked in subclasses of State after request has been acknowledged by the target actor
     * to add additional events (e.g. a non-blocking Task can complete itself).
     */
    public void handleReceipt(ActorRequestDeliveryReceipt receipt) {
        // Hook that can be invoked in State class
    }

    public abstract void handleFailure(ActorRequestFailure failure);

    public String getDescription() {
        return this.getClass().getSimpleName() + "[" + targetActorId + "]";
    }

    public final void updateState(ModelActorReplyEvent event) {
        Request request = requests.computeIfAbsent(event.getCorrelationId(), k -> new Request(this));
        request.updateState(event);
    }

    public final void recoveryCompleted() {
        requests.values().forEach(Request::recoveryCompleted);
    }

    protected void requestDeliveryFailed(Request request) {
    }
}
