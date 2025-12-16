package org.cafienne.model.dmn.definition;

import org.cafienne.model.cmmn.definition.CMMNElementDefinition;
import org.cafienne.model.cmmn.definition.ModelDefinition;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class RuleDefinition extends DMNElementDefinition {
    private final List<RuleEntryDefinition> inputs = new ArrayList<>();
    private final List<RuleEntryDefinition> outputs = new ArrayList<>();
    private final String description;


    public RuleDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement, false);
        parse("inputEntry", RuleEntryDefinition.class, inputs);
        parse("outputEntry", RuleEntryDefinition.class, outputs);
        description = parseString("description", false);
    }

    @Override
    public boolean equalsWith(Object object) {
        return equalsWith(object, this::sameTable);
    }

    public boolean sameTable(RuleDefinition other) {
        return sameIdentifiers(other) &&
                same(this.inputs, other.inputs) &&
                same(this.outputs, other.outputs);
    }
}
