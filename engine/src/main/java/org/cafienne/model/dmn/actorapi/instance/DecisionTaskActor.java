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

package org.cafienne.model.dmn.actorapi.instance;

import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ActorType;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.communication.request.response.ActorRequestFailure;
import org.cafienne.actormodel.communication.request.state.RemoteActorState;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.actormodel.message.event.ModelEvent;
import org.cafienne.model.cmmn.actorapi.command.plan.task.CompleteTask;
import org.cafienne.model.cmmn.actorapi.command.plan.task.FailTask;
import org.cafienne.model.dmn.actorapi.command.StartDecision;
import org.cafienne.model.dmn.actorapi.event.*;
import org.cafienne.model.dmn.definition.DecisionModelDefinition;
import org.cafienne.model.dmn.invocation.DMNInvocation;
import org.cafienne.model.processtask.actorapi.command.ProcessCommand;
import org.cafienne.model.processtask.actorapi.event.ProcessEvent;
import org.cafienne.system.CaseSystem;
import org.cafienne.util.json.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecisionTaskActor extends ModelActor {

    private final static Logger logger = LoggerFactory.getLogger(DecisionTaskActor.class);
    private DecisionModelDefinition definition;
    private String name;
    private String parentActorId;
    private String rootActorId;
    private DMNInvocation dmnInvocation;
    private ValueMap inputParameters;
    private ValueMap outputParameters;
    private ParentProcessTaskState processTaskState; // Can only be created when the parent invokes us

    public DecisionTaskActor(CaseSystem caseSystem) {
        super(caseSystem);
    }

    @Override
    public ActorMetadata metadata() {
        return new ActorMetadata(ActorType.Process, getId(), null);
    }

    @Override
    protected boolean supportsCommand(Object msg) {
        return msg instanceof ProcessCommand;
    }

    @Override
    protected boolean supportsEvent(ModelEvent msg) {
        return msg instanceof ProcessEvent;
    }

    public DecisionModelDefinition getDefinition() {
        return definition;
    }

    private void setDefinition(DecisionModelDefinition definition) {
        this.definition = definition;
    }

    @Override
    public String getParentActorId() {
        return parentActorId;
    }

    @Override
    public String getRootActorId() {
        return rootActorId;
    }

    public ValueMap getMappedInputParameters() {
        return inputParameters;
    }

    public String getName() {
        return name;
    }

    public DMNInvocation getImplementation() {
        return  dmnInvocation;
    }

    public void handleStartProcessCommand(StartDecision command) {
//        this.processTaskState = new ParentProcessTaskState(this);
        this.addEvent(new DecisionStarted(this, command));
    }

    public void updateState(DecisionStarted event) {
        this.parentActorId = event.parentActorId;
        this.processTaskState = new ParentProcessTaskState(this);
        this.setEngineVersion(event.engineVersion);
        this.setDebugMode(event.debugMode);
        this.definition = event.definition;
        this.dmnInvocation = definition.createInstance(event.);
        this.name = event.name;
        this.parentActorId = event.parentActorId;
        this.rootActorId = event.rootActorId;
        this.inputParameters = event.inputParameters;
        if (!recoveryRunning()) {
            addDebugInfo(() -> "Starting process task " + name + " with input: ", inputParameters);
            getImplementation().start();
        }
    }

    public void updateState(DecisionReactivated event) {
        this.dmnInvocation = definition.createInstance(this);
        this.inputParameters = event.inputParameters;
        if (!recoveryRunning()) {
            addDebugInfo(() -> "Reactivating process " + getName());
            getImplementation().resetOutput();
            getImplementation().reactivate();
        }
    }

    public void updateState(DecisionSuspended event) {
        if (!recoveryRunning()) {
            addDebugInfo(() -> "Suspending process " + getName());
            getImplementation().suspend();
        }
    }

    public void updateState(DecisionResumed event) {
        if (!recoveryRunning()) {
            addDebugInfo(() -> "Resuming process " + getName());
            getImplementation().resume();
        }
    }

    public void updateState(DecisionTerminated event) {
        if (!recoveryRunning()) {
            addDebugInfo(() -> "Terminating process " + getName());
            getImplementation().terminate();
        }
    }

    public void updateState(DecisionCompleted event) {
        this.outputParameters = event.output;
        addDebugInfo(() -> "Completing process task " + name + " of process type " + getImplementation().getClass().getName() + " with output:", outputParameters);
        if (recoveryFinished()) {
            processTaskState.inform(new CompleteTask(this, outputParameters));
        }
    }

    public void updateState(DecisionFailed event) {
        outputParameters = event.output;
        processTaskState.inform(new FailTask(this, outputParameters));

//                , failure -> {
//            logger.error("Could not complete process task " + getId() + " " + name + " in parent, due to:\n" + failure);
//        }, success -> {
//            addDebugInfo(() -> "Reporting failure of process task " + getId() + " " + name + " in parent was accepted");
//        });
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    public void completed(ValueMap processOutputParameters) {
        addEvent(new DecisionCompleted(this, processOutputParameters));
    }

    public void failed(String errorDescription, ValueMap processOutputParameters) {
        addDebugInfo(() -> "Encountered failure in process task '" + name + "' of process type " + getImplementation().getClass().getName());
        addDebugInfo(() -> "Error: " + errorDescription);
        addDebugInfo(() -> "Output: ", processOutputParameters);
        addEvent(new DecisionFailed(this, processOutputParameters));
    }

    public void failed(ValueMap processOutputParameters) {
        addDebugInfo(() -> "Reporting failure in process task " + name + " of process type " + getImplementation().getClass().getName() + " with output: ", processOutputParameters);
        addEvent(new DecisionFailed(this, processOutputParameters));
    }

    @Override
    protected void addCommitEvent(ModelCommand message) {
        addEvent(new DecisionModified(this, message));
    }


    private static class ParentProcessTaskState extends RemoteActorState<DecisionTaskActor> {

        public ParentProcessTaskState(DecisionTaskActor actor) {
            super(actor, new ActorMetadata(ActorType.Case, actor.getParentActorId(), null));
        }

        private void inform(ModelCommand command) {
            if (targetActorId.isEmpty()) {
                // No need to inform about our transitions.
                return;
            }
            sendRequest(command);
//            failure -> {
//                actor.addDebugInfo(() -> "Could not complete process task " + getId() + " " + name + " in parent, due to:", failure.toJson());
//                logger.error("Could not complete process task " + getId() + " " + name + " in parent, due to:\n" + failure);
//            },
//                    success -> addDebugInfo(() -> "Completed process task " + getId() + " '" + name + "' in parent " + parentActorId));
        }

        @Override
        public void handleFailure(ActorRequestFailure failure) {
            actor.addDebugInfo(() -> "Could not complete process task " + actor.getId() + " " + actor.name + " in parent, due to:", failure.toJson());
            logger.error("Could not complete process task " + actor.getId() + " " + actor.name + " in parent, due to:\n" + failure);
        }
    }
}
