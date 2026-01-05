/*
 * Copyright (C) 2014  Batav B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cafienne.actormodel.communication.serializers;

import org.cafienne.actormodel.communication.receiver.command.RunActorRequest;
import org.cafienne.actormodel.communication.receiver.event.ActorRequestExecuted;
import org.cafienne.actormodel.communication.receiver.event.ActorRequestFailed;
import org.cafienne.actormodel.communication.receiver.event.ActorRequestStored;
import org.cafienne.actormodel.communication.receiver.reply.ActorRequestDeliveryReceipt;
import org.cafienne.actormodel.communication.receiver.reply.ActorRequestFailure;
import org.cafienne.actormodel.communication.sender.command.ActorRequestDeliveryFailure;
import org.cafienne.actormodel.communication.sender.command.RequestModelActor;
import org.cafienne.actormodel.communication.sender.event.ActorRequestCreated;
import org.cafienne.actormodel.communication.sender.event.ActorRequestDelivered;
import org.cafienne.actormodel.communication.sender.event.ActorRequestNotDelivered;
import org.cafienne.infrastructure.serialization.CafienneSerializer;

public class CommunicationSerializers {
    public static void register() {
        CafienneSerializer.addManifestWrapper(RequestModelActor.class, RequestModelActor::new);
        CafienneSerializer.addManifestWrapper(RunActorRequest.class, RunActorRequest::new);
        CafienneSerializer.addManifestWrapper(ActorRequestDeliveryFailure.class, ActorRequestDeliveryFailure::new);
        CafienneSerializer.addManifestWrapper(ActorRequestCreated.class, ActorRequestCreated::new);
        CafienneSerializer.addManifestWrapper(ActorRequestDelivered.class, ActorRequestDelivered::new);
        CafienneSerializer.addManifestWrapper(ActorRequestDeliveryReceipt.class, ActorRequestDeliveryReceipt::new);
        CafienneSerializer.addManifestWrapper(ActorRequestStored.class, ActorRequestStored::new);
        CafienneSerializer.addManifestWrapper(ActorRequestFailure.class, ActorRequestFailure::new);
        CafienneSerializer.addManifestWrapper(ActorRequestExecuted.class, ActorRequestExecuted::new);
        CafienneSerializer.addManifestWrapper(ActorRequestFailed.class, ActorRequestFailed::new);
        CafienneSerializer.addManifestWrapper(ActorRequestNotDelivered.class, ActorRequestNotDelivered::new);
    }
}
