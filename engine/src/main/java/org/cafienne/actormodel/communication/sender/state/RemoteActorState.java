package org.cafienne.actormodel.communication.sender.state;

import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.communication.receiver.reply.ActorRequestDeliveryReceipt;
import org.cafienne.actormodel.communication.receiver.reply.ActorRequestFailure;
import org.cafienne.actormodel.communication.sender.command.ActorRequestDeliveryFailure;
import org.cafienne.actormodel.communication.sender.event.ActorRequestCreated;
import org.cafienne.actormodel.communication.sender.event.ActorRequestDelivered;
import org.cafienne.actormodel.communication.sender.event.ActorRequestNotDelivered;
import org.cafienne.actormodel.communication.sender.event.ModelActorReplyEvent;
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
    public final ActorMetadata receiver;
    private final Map<String, Request> requests = new HashMap<>();

    protected RemoteActorState(LocalActor actor, ActorMetadata receiver) {
        this.actor = actor;
        this.receiver = receiver;
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
     * Hook that can be invoked in subclasses of State after request has been acknowledged by the receiving actor
     * to add additional events (e.g. a non-blocking Task can complete itself).
     */
    public void handleReceipt(ActorRequestDeliveryReceipt receipt) {
        // Hook that can be invoked in State class
    }

    public abstract void handleNotDelivered(ActorRequestNotDelivered notDelivered);

    public abstract void handleFailure(ActorRequestFailure failure);

    public String getDescription() {
        return this.getClass().getSimpleName() + "[" + receiver.actorId() + "]";
    }

    public final void updateState(ActorRequestCreated event) {
        Request request = new Request(this);
        requests.put(event.getCorrelationId(), request);
        request.created(event);
    }

    public final void updateState(ModelActorReplyEvent event) {
        Request request = requests.computeIfAbsent(event.getCorrelationId(), k -> new Request(this));
        request.updateState(event);
    }

    public final void recoveryCompleted() {
        requests.values().forEach(Request::recoveryCompleted);
    }

    final void requestDeliveryFailed(Request request, String reason) {
        // Also do logging
        ActorRequestDeliveryFailure failure = new ActorRequestDeliveryFailure(actor, receiver, request.getCommand(), reason);
        actor.self().tell(failure, actor.self());
    }
}
