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

package org.cafienne.model.cmmn.instance;

import org.cafienne.actormodel.communication.receiver.reply.ActorRequestFailure;
import org.cafienne.actormodel.communication.sender.event.ActorRequestNotDelivered;
import org.cafienne.actormodel.communication.sender.state.RemoteActorState;
import org.cafienne.model.cmmn.actorapi.command.CaseCommand;
import org.cafienne.model.cmmn.actorapi.command.plan.task.CompleteTask;
import org.cafienne.model.cmmn.actorapi.command.plan.task.FailTask;
import org.cafienne.model.cmmn.actorapi.command.plan.task.HandleTaskImplementationTransition;
import org.cafienne.model.cmmn.actorapi.event.CaseOutputFilled;
import org.cafienne.model.cmmn.definition.CasePlanDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class CasePlan extends Stage<CasePlanDefinition> {
    private final static Logger logger = LoggerFactory.getLogger(CasePlan.class);
    private final ParentCaseTaskState parentTaskState;

    public CasePlan(String id, CasePlanDefinition definition, Case caseInstance) {
        super(id, 0, definition, definition, null, caseInstance, StateMachine.CasePlan);
        this.parentTaskState = caseInstance.metadata.hasParent() ? new ParentCaseTaskState(this) : null;
    }

    private void inform(Transition transition) {
        inform(() -> new HandleTaskImplementationTransition(getCaseInstance(), transition));
    }

    private void inform(Supplier<CaseCommand> creator) {
        if (this.parentTaskState != null) {
            // Only inform about our transitions if we have a parent.
            this.parentTaskState.sendRequest(creator.get());
        }
    }

    @Override
    protected void suspendInstance() {
        super.suspendInstance();
        inform(Transition.Suspend);
    }

    @Override
    protected void reactivateInstance() {
        super.reactivateInstance();
        inform(Transition.Reactivate);
    }

    @Override
    protected void completeInstance() {
        super.completeInstance();
        addEvent(new CaseOutputFilled(getCaseInstance(), getCaseInstance().getOutputParameters()));
        inform(() -> new CompleteTask(getCaseInstance(), getCaseInstance().getOutputParameters()));
    }

    @Override
    protected void terminateInstance() {
        super.terminateInstance();
        inform(Transition.Terminate);
    }

    @Override
    protected void failInstance() {
        super.failInstance();
        inform(() -> new FailTask(getCaseInstance(), getCaseInstance().getOutputParameters()));
    }

    @Override
    public void migrateDefinition(CasePlanDefinition newDefinition, boolean skipLogic) {
        addDebugInfo(() -> "\nMigrating Case Plan");
        migrateItemDefinition(newDefinition, newDefinition, skipLogic);
        addDebugInfo(() -> "Completed Case Plan migration\n");
    }

    private static class ParentCaseTaskState extends RemoteActorState<Case> {
        private final CasePlan plan;

        public ParentCaseTaskState(CasePlan plan) {
            super(plan.getCaseInstance(), plan.getCaseInstance().metadata.parent());
            this.plan = plan;
        }

        @Override
        public void handleFailure(ActorRequestFailure failure) {
            // TTD: this needs better handling

            // Wow, now what? CaseTask did not accept our information, but why??
            //  And... should we handle this by e.g. going to Fault state? Or what? Can we make this inconsistency clear somehow other than through the log file? Generate a special event or so?
            logger.error("Parent case {} did not accept our request {} and responded with a failure\n{}", plan.getCaseInstance().getParentActorId(), failure.command, failure);
        }

        @Override
        public void handleNotDelivered(ActorRequestNotDelivered notDelivered) {
            actor.addDebugInfo(() -> "Could not complete case task for " + actor.metadata + " in parent, due to:" + notDelivered.errorMessage);
        }
    }
}

