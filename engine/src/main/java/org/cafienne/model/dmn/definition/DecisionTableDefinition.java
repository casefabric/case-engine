package org.cafienne.model.dmn.definition;

import org.cafienne.model.cmmn.definition.CMMNElementDefinition;
import org.cafienne.model.cmmn.definition.ModelDefinition;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class DecisionTableDefinition extends DMNElementDefinition {
    private final List<ParameterDefinition> inputs = new ArrayList<>();
    private final List<ParameterDefinition> outputs = new ArrayList<>();


    public DecisionTableDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement, false);
        parse("input", ParameterDefinition.class, inputs);
        parse("output", ParameterDefinition.class, outputs);
    }

    @Override
    public boolean equalsWith(Object object) {
        return equalsWith(object, this::sameTable);
    }

    public boolean sameTable(DecisionTableDefinition other) {
        return sameIdentifiers(other) &&
                same(this.inputs, other.inputs) &&
                same(this.outputs, other.outputs);
    }
}
