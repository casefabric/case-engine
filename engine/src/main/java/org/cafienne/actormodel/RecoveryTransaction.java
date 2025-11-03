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

import org.cafienne.actormodel.message.event.CaseSystemEvent;
import org.cafienne.actormodel.message.event.ModelEvent;
import org.cafienne.infrastructure.enginedeveloper.EngineDeveloperConsole;

public class RecoveryTransaction extends UserMessageTransaction<ModelEvent> {
    RecoveryTransaction(ModelActor actor, Reception reception, ModelEvent event) {
        super(actor, reception, event);
    }

    @Override
    void perform() {
        // First check if our actor can handle the event
        if (!(actor.supportsEvent(message) || message instanceof CaseSystemEvent)) {
            // Step 1. Weird: ModelEvents in recovery of other models??
            reception.reportInvalidRecoveryEvent(message);
            return;
        }

        if (this.message.isBootstrapMessage()) {
            // Tell the actor about the first message, so that it can do initialization kind of stuff
            actor.handleBootstrapMessage(this.message.asBootstrapMessage());
        }
//        System.out.println("Recovering event " + actor + ".[" + actor.lastSequenceNr()+ "].[" +  event.getTimestamp().toString().substring(0, 23) +"].[" + event.getClass().getSimpleName()+ "]");
        if (actor.getLogger().isDebugEnabled()) {
            actor.getLogger().debug("Recovering event {}.[{}].[{}].[{}]", actor, actor.lastSequenceNr(), this.message.getTimestamp().toString().substring(0, 23), this.message.getClass().getSimpleName());
        }
        try {
            this.message.updateActorState(actor);
            if (this.message.isBootstrapMessage()) {
                reception.unlock();
            }
        } catch (Throwable throwable) {
            reception.reportStateUpdateFailure(throwable);
        }
    }

    @Override
    void addEvent(ModelEvent event) {
        // Recovery is still running.

        // NOTE: ModelActors should not generate events during recovery.
        //  Such has been implemented for TenantActor and ProcessTaskActor, and partly for Case.
        //  Enabling the logging will showcase where this pattern has not been completely done.
        if (EngineDeveloperConsole.enabled()) {
            EngineDeveloperConsole.debugIndentedConsoleLogging("!!! Recovering " + actor + " on event of type " + message.getClass().getSimpleName() +" generates event of type " + event.getClass().getSimpleName());
        }
    }
}
