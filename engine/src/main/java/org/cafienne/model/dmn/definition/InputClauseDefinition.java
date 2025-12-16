package org.cafienne.model.dmn.definition;

import org.cafienne.model.cmmn.definition.CMMNElementDefinition;
import org.cafienne.model.cmmn.definition.ModelDefinition;
import org.w3c.dom.Element;

public class InputClauseDefinition extends DMNElementDefinition {
    String typeRef;

    public InputClauseDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement, true);

        this.typeRef = parseAttribute("typeRef",  false);
    }

    @Override
    public boolean equalsWith(Object object) {
        return equalsWith(object, this::sameClause);
    }

    public boolean sameClause(InputClauseDefinition other) {
        return sameIdentifiers(other) &&
                same(this.typeRef, other.typeRef);
    }
}
