package org.cafienne.model.dmn.definition;

import org.cafienne.model.cmmn.definition.CMMNElementDefinition;
import org.cafienne.model.cmmn.definition.ModelDefinition;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class DecisionTableDefinition extends DMNElementDefinition {
    private final List<InputClauseDefinition> input = new ArrayList<>();
    private final List<OutputClauseDefinition> output = new ArrayList<>();
    private final List<DecisionRuleDefinition> rule = new ArrayList<>();
    private final HitPolicyDefinition hitPolicy;


    public DecisionTableDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement, false);
        parse("input", InputClauseDefinition.class, input);
        parse("output", OutputClauseDefinition.class, output);
        parse("rule", DecisionRuleDefinition.class, rule);

        this.hitPolicy = HitPolicyDefinition.fromString(parseAttribute("hitPolicy", false));
    }

    public List<InputClauseDefinition> getInput() {
        return input;
    }

    public List<OutputClauseDefinition> getOutput() {
        return output;
    }

    public List<DecisionRuleDefinition> getRule() {
        return rule;
    }

    public HitPolicyDefinition getHitPolicy() {
        return hitPolicy;
    }

    @Override
    public boolean equalsWith(Object object) {
        return equalsWith(object, this::sameTable);
    }

    public boolean sameTable(DecisionTableDefinition other) {
        return sameIdentifiers(other) &&
                same(this.input, other.input) &&
                same(this.output, other.output) &&
                same(this.rule, other.rule);
    }
}
