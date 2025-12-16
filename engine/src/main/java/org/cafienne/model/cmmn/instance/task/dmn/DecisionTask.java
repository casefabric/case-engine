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

package org.cafienne.model.cmmn.instance.task.dmn;

import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ActorType;
import org.cafienne.model.cmmn.definition.DecisionTaskDefinition;
import org.cafienne.model.cmmn.definition.ItemDefinition;
import org.cafienne.model.cmmn.instance.Stage;
import org.cafienne.model.cmmn.instance.Task;
import org.cafienne.model.dmn.invocation.DMNInvocation;
import org.cafienne.util.json.ValueMap;

public class DecisionTask extends Task<DecisionTaskDefinition> {
    private final DMNInvocation implementation;

    public DecisionTask(String id, int index, ItemDefinition itemDefinition, DecisionTaskDefinition definition, Stage<?> stage) {
        super(id, index, itemDefinition, definition, stage);
        implementation = definition.getImplementationDefinition().createInstance(this);
    }
    @Override
    protected ActorMetadata target() {
        // TODO change ActorType to NO ACTOR
        return new ActorMetadata(ActorType.ModelActor, getId(), getCaseInstance().metadata());
    }

    @Override
    protected void startImplementation(ValueMap inputParameters) {
        implementation.start();
    }

    @Override
    protected void suspendImplementation() {

    }

    @Override
    protected void resumeImplementation() {

    }

    @Override
    protected void terminateImplementation() {

    }

    @Override
    protected void reactivateImplementation(ValueMap inputParameters) {
        implementation.start();
    }

    @Override
    protected void lostDefinition() {
        addDebugInfo(() -> "Dropping ProcessTasks through migration is not possible. Task[" + getPath() + "] remains in the case with current state '" + getState() + "'");
    }
}
