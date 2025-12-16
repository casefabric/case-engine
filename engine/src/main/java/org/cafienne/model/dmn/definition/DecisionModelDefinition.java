package org.cafienne.model.dmn.definition;

import org.cafienne.model.cmmn.definition.DefinitionsDocument;
import org.cafienne.model.cmmn.definition.ModelDefinition;
import org.w3c.dom.Element;

public class DecisionModelDefinition  extends ModelDefinition {
    private final DecisionDefinition decision;

    public DecisionModelDefinition(Element definitionElement, DefinitionsDocument document) {
        super(definitionElement, document);

        this.decision = new DecisionDefinition(definitionElement, this, this);
   }

    @Override
    public boolean equalsWith(Object object) {
        return equalsWith(object, this::sameDecisionDefinition);
    }

    public boolean sameDecisionDefinition(DecisionModelDefinition other) {
        return sameModelDefinition(other) &&
                same(this.decision, other.decision);
    }
}
