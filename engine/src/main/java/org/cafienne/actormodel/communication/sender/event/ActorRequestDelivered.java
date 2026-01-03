package org.cafienne.actormodel.communication.sender.event;

import org.cafienne.actormodel.communication.CaseSystemCommitEvent;
import org.cafienne.actormodel.communication.receiver.reply.ActorRequestDeliveryReceipt;
import org.cafienne.actormodel.communication.sender.state.RemoteActorState;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.util.json.ValueMap;

@Manifest
public class ActorRequestDelivered extends ModelActorReplyEvent implements CaseSystemCommitEvent {
    // IMPORTANT: ActorRequestDelivered is also a CommitEvent.
    //  In case of non-blocking tasks, when this event is received an additional PlanItemTransitioned event is generated
    //  indicating that the task is completed (in the context of the parent case).
    //  This event will be created on top of this commit event in the same transaction, so in that sense,
    //  this commit event does not complete the transaction.
    //  But, this is taken care of in the ModelActorTransaction, since that will add a CaseModified event.
    //  Also, note that in the case of blocking tasks, the additional PlanItemTransitioned event will not be
    //  generated and in that scenario also no additional CaseModified will be generated.

    public ActorRequestDelivered(RemoteActorState<?> state, ActorRequestDeliveryReceipt receipt) {
        super(state.actor, receipt.getCorrelationId(), state.receiver);
    }

    public ActorRequestDelivered(ValueMap json) {
        super(json);
    }
}
