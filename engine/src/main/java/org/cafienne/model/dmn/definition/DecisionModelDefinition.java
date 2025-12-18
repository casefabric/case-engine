package org.cafienne.model.dmn.definition;

import org.cafienne.model.cmmn.definition.DefinitionsDocument;
import org.cafienne.model.cmmn.definition.ModelDefinition;
import org.cafienne.model.cmmn.definition.task.TaskImplementationContract;
import org.cafienne.model.cmmn.instance.task.decision.DecisionTask;
import org.cafienne.model.dmn.invocation.DMNInvocation;
import org.w3c.dom.Element;

public class DecisionModelDefinition  extends ModelDefinition
    implements TaskImplementationContract {
    private final DecisionDefinition decision;

    public DecisionModelDefinition(Element definitionElement, DefinitionsDocument document) {
        super(definitionElement, document);

        this.decision = new DecisionDefinition(definitionElement, this, this);
   }

   public DecisionDefinition getDecision() {
        return decision;
   }

    @Override
    public boolean equalsWith(Object object) {
        return equalsWith(object, this::sameDecisionDefinition);
    }

    public boolean sameDecisionDefinition(DecisionModelDefinition other) {
        return sameModelDefinition(other) &&
                same(this.decision, other.decision);
    }

    public DMNInvocation createInstance(DecisionTask task) {
        return new DMNInvocation(task, this);
    }
}
