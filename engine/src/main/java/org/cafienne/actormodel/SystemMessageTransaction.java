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

package org.cafienne.actormodel;

import org.apache.pekko.persistence.JournalProtocol;
import org.apache.pekko.persistence.RecoveryCompleted;
import org.apache.pekko.persistence.SnapshotOffer;
import org.apache.pekko.persistence.SnapshotProtocol;
import org.cafienne.actormodel.identity.UserIdentity;
import org.cafienne.actormodel.message.event.ModelEvent;
import org.cafienne.infrastructure.enginedeveloper.EngineDeveloperConsole;
import org.cafienne.infrastructure.serialization.DeserializationFailure;

public class SystemMessageTransaction extends MessageTransaction<Object> {

    SystemMessageTransaction(ModelActor actor, Reception reception, Object message) {
        super(actor, reception, message);
    }

    @Override
    void perform() {
        switch (message) {
            // Step 1. All looks good, just open the front door
            case RecoveryCompleted recoveryCompleted -> {
                if (actor.getLogger().isDebugEnabled()) {
                    actor.getLogger().debug("{} completed recovery", actor);
                }
//                System.out.println("\n\n============ COMPLETED RECOVERY IN " + actor);
                reception.open();
            }
            // Step 2. Probably incompatible change in event serialization format. Big issue
            case DeserializationFailure deserializationFailure -> reception.reportDeserializationFailure(deserializationFailure);
            // Step 3. Could be a snapshot (although currently no ModelActor uses it)
            case SnapshotOffer snapshotOffer -> actor.handleSnapshot(snapshotOffer);
            case SnapshotProtocol.Message snapshotMsg -> actor.handleSnapshotProtocolMessage(snapshotMsg);
            case JournalProtocol.Message journalMsg -> actor.handleJournalProtocolMessage(journalMsg);
            // Step 4. Some unclear incoming message, each printing a different log message depending on the actor state
            case null -> handleNullMessage();
            default -> handleUnknownMessage();
        }
    }

    private void handleNullMessage() {
        if (actor.recoveryRunning()) {
            actor.getLogger().warn("{} received a null Object during recovery", actor);
        } else {
            actor.getLogger().warn("{} received a null message from {}", actor, actor.sender());
        }
    }

    private void handleUnknownMessage() {
        if (actor.recoveryRunning()) {
            actor.getLogger().warn("{} received unknown message of type {} during recovery: {}", actor, message.getClass().getName(), message);
        } else {
            actor.getLogger().warn("{} received a message it cannot handle, of type {} from {}", actor, message.getClass().getName(), actor.sender());
        }
    }

    @Override
    void addEvent(ModelEvent event) {
        // NOTE: ModelActors should not generate events during recovery.
        //  Such has been implemented for TenantActor and ProcessTaskActor, and partly for Case.
        //  Enabling the logging will showcase where this pattern has not been completely done.
        if (EngineDeveloperConsole.enabled()) {
            EngineDeveloperConsole.debugIndentedConsoleLogging("!!! System Message Transaction on " + actor + " generates event of type " + event.getClass().getSimpleName());
        }
    }

    @Override
    public String getCorrelationId() {
        return "";
    }

    @Override
    public UserIdentity getUser() {
        return null;
    }
}
