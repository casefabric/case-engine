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

package org.cafienne.model.cmmn.definition;

import org.cafienne.model.cmmn.instance.Case;
import org.cafienne.model.cmmn.instance.PlanItemType;
import org.cafienne.model.cmmn.instance.Stage;
import org.cafienne.model.cmmn.instance.task.dmn.DecisionTask;
import org.cafienne.model.dmn.definition.DecisionModelDefinition;
import org.w3c.dom.Element;

public class DecisionTaskDefinition extends TaskDefinition<DecisionModelDefinition> {
    private final String decisionRef;
    private DecisionModelDefinition decisionModelDefinition;

    public DecisionTaskDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement);
        this.decisionRef = parseAttribute("decisionRef", true);
    }

    @Override
    public PlanItemType getItemType() {
        return PlanItemType.DecisionTask;
    }

    @Override
    protected void resolveReferences() {
        super.resolveReferences();
        this.decisionModelDefinition = getCaseDefinition().getDefinitionsDocument().getDecisionModelDefinition(this.decisionRef);
        if (this.decisionModelDefinition == null) {
            getModelDefinition().addReferenceError("The process task '" + this.getName() + "' refers to a process named " + decisionRef + ", but that definition is not found");
            return; // Avoid further checking on this element
        }
    }

    @Override
    public DecisionTask createInstance(String id, int index, ItemDefinition itemDefinition, Stage<?> stage, Case caseInstance) {
        return new DecisionTask(id, index, itemDefinition, this, stage);
    }

    @Override
    public DecisionModelDefinition getImplementationDefinition() {
        return decisionModelDefinition;
    }

    @Override
    public boolean equalsWith(Object object) {
        return equalsWith(object, this::sameDecisionTask);
    }

    public boolean sameDecisionTask(DecisionTaskDefinition other) {
        return sameTask(other)
                && decisionModelDefinition.sameDecisionDefinition(other.decisionModelDefinition);
    }
}
