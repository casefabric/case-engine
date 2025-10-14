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

package org.cafienne.actormodel.serializers;

import org.cafienne.actormodel.communication.reply.command.RunActorRequest;
import org.cafienne.actormodel.communication.reply.event.ActorRequestExecuted;
import org.cafienne.actormodel.communication.reply.event.ActorRequestFailed;
import org.cafienne.actormodel.communication.reply.event.ActorRequestStored;
import org.cafienne.actormodel.communication.request.command.RequestModelActor;
import org.cafienne.actormodel.communication.request.event.ActorRequestCreated;
import org.cafienne.actormodel.communication.request.event.ActorRequestDelivered;
import org.cafienne.actormodel.communication.request.response.ActorRequestDeliveryReceipt;
import org.cafienne.actormodel.communication.request.response.ActorRequestFailure;
import org.cafienne.actormodel.message.event.DebugEvent;
import org.cafienne.actormodel.message.event.SentryEvent;
import org.cafienne.actormodel.message.response.*;
import org.cafienne.infrastructure.serialization.CafienneSerializer;
import org.cafienne.model.cmmn.actorapi.event.DebugDisabled;
import org.cafienne.model.cmmn.actorapi.event.DebugEnabled;

public class BaseSerializers {
    public static void register() {
        registerBaseCommands();
        registerBaseEvents();
        registerBaseResponses();
    }

    private static void registerBaseCommands() {
        CafienneSerializer.addManifestWrapper(RequestModelActor.class, RequestModelActor::new);
        CafienneSerializer.addManifestWrapper(RunActorRequest.class, RunActorRequest::new);
        CafienneSerializer.addManifestWrapper(ActorRequestCreated.class, ActorRequestCreated::new);
        CafienneSerializer.addManifestWrapper(ActorRequestDelivered.class, ActorRequestDelivered::new);
        CafienneSerializer.addManifestWrapper(ActorRequestDeliveryReceipt.class, ActorRequestDeliveryReceipt::new);
        CafienneSerializer.addManifestWrapper(ActorRequestStored.class, ActorRequestStored::new);
        CafienneSerializer.addManifestWrapper(ActorRequestFailure.class, ActorRequestFailure::new);
        CafienneSerializer.addManifestWrapper(ActorRequestExecuted.class, ActorRequestExecuted::new);
        CafienneSerializer.addManifestWrapper(ActorRequestFailed.class, ActorRequestFailed::new);
    }

    private static void registerBaseEvents() {
        CafienneSerializer.addManifestWrapper(DebugEvent.class, DebugEvent::new);
        CafienneSerializer.addManifestWrapper(SentryEvent.class, SentryEvent::new);
        CafienneSerializer.addManifestWrapper(DebugDisabled.class, DebugDisabled::new);
        CafienneSerializer.addManifestWrapper(DebugEnabled.class, DebugEnabled::new);
    }

    private static void registerBaseResponses() {
        CafienneSerializer.addManifestWrapper(CommandFailure.class, CommandFailure::new);
        CafienneSerializer.addManifestWrapper(SecurityFailure.class, SecurityFailure::new);
        CafienneSerializer.addManifestWrapper(ActorChokedFailure.class, ActorChokedFailure::new);
        CafienneSerializer.addManifestWrapper(ActorExistsFailure.class, ActorExistsFailure::new);
        CafienneSerializer.addManifestWrapper(ActorInStorage.class, ActorInStorage::new);
        CafienneSerializer.addManifestWrapper(EngineChokedFailure.class, EngineChokedFailure::new);
    }
}
