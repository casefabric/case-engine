package org.cafienne.model.dmn.definition;

import org.cafienne.model.cmmn.definition.CMMNElementDefinition;
import org.cafienne.model.cmmn.definition.ModelDefinition;
import org.w3c.dom.Element;

import javax.annotation.Nullable;

public class RuleEntryDefinition extends DMNElementDefinition {
    @Nullable
    String text;


    public RuleEntryDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement, false);
        text = parseString("text", false);
    }

    @Override
    public boolean equalsWith(Object object) {
        return equalsWith(object, this::sameTable);
    }

    public boolean sameTable(RuleEntryDefinition other) {
        return sameIdentifiers(other) &&
                same(this.text, other.text);
    }
}
