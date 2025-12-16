package org.cafienne.model.dmn.definition;

import org.cafienne.model.cmmn.definition.CMMNElementDefinition;
import org.cafienne.model.cmmn.definition.ModelDefinition;
import org.cafienne.model.cmmn.expression.spel.Evaluator;
import org.cafienne.model.cmmn.instance.task.dmn.DecisionTask;
import org.cafienne.util.json.ValueMap;
import org.w3c.dom.Element;

import javax.annotation.Nullable;

public class UnaryTestsDefinition extends DMNElementDefinition {
    @Nullable
    private final String text;
    private final Evaluator evaluator;

    public UnaryTestsDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement, false);
        this.text = parseString("text", false);

        this.evaluator = new Evaluator(this, this.text);
    }

    @Nullable
    public String getText() {
        return text;
    }

    @Override
    public boolean equalsWith(Object object) {
        return equalsWith(object, this::sameUnaryTests);
    }

    public boolean sameUnaryTests(UnaryTestsDefinition other) {
        return sameIdentifiers(other) &&
                same(this.text, other.text);
    }

    public boolean match(ValueMap taskInput, DecisionTask task) {
        var context = new DMNRootAPIObject(taskInput, task);
        return this.evaluator.evaluate(context);
    }
}
