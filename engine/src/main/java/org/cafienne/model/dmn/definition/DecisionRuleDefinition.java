package org.cafienne.model.dmn.definition;

import org.cafienne.model.cmmn.definition.CMMNElementDefinition;
import org.cafienne.model.cmmn.definition.ModelDefinition;
import org.cafienne.model.cmmn.instance.task.dmn.DecisionTask;
import org.cafienne.util.json.ValueMap;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class DecisionRuleDefinition extends DMNElementDefinition {
    private final List<UnaryTestsDefinition> inputsEntries = new ArrayList<>();
    private final List<LiteralExpressionDefinition> outputsEntries = new ArrayList<>();
    private final String description;


    public DecisionRuleDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement, false);
        parse("inputEntry", UnaryTestsDefinition.class, inputsEntries);
        parse("outputEntry", LiteralExpressionDefinition.class, outputsEntries);
        description = parseString("description", false);
    }

    public List<UnaryTestsDefinition> getInputEntries() {
        return inputsEntries;
    }

    public List<LiteralExpressionDefinition> getOutputEntries() {
        return outputsEntries;
    }

    @Override
    public boolean equalsWith(Object object) {
        return equalsWith(object, this::sameTable);
    }

    public boolean sameTable(DecisionRuleDefinition other) {
        return sameIdentifiers(other) &&
                same(this.inputsEntries, other.inputsEntries) &&
                same(this.outputsEntries, other.outputsEntries);
    }

    public boolean isApplicable(ValueMap taskInput, DecisionTask task) {
        return inputsEntries.stream().allMatch(input -> input.match(taskInput, task));
    }

    public Object[] getOutputs(ValueMap taskInput, DecisionTask task) {
        return outputsEntries.stream().map(
                entry -> entry.getValue(taskInput, task)).toArray();
    }
}
