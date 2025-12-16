package org.cafienne.model.dmn.definition;

import org.cafienne.model.cmmn.definition.CMMNElementDefinition;
import org.cafienne.model.cmmn.definition.ModelDefinition;
import org.cafienne.model.cmmn.expression.spel.Evaluator;
import org.cafienne.model.cmmn.instance.task.dmn.DecisionTask;
import org.cafienne.util.json.ValueMap;
import org.w3c.dom.Element;

import javax.annotation.Nullable;

public class LiteralExpressionDefinition extends DMNElementDefinition {
    private final Evaluator evaluator;
    @Nullable
    String text;

    public LiteralExpressionDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement, false);
        text = parseString("text", false);

        this.evaluator = new Evaluator(this, this.text);
    }

    @Nullable
    public String getText() {
        return text;
    }

    @Override
    public boolean equalsWith(Object object) {
        return equalsWith(object, this::sameLiteralExpression);
    }

    public boolean sameLiteralExpression(LiteralExpressionDefinition other) {
        return sameIdentifiers(other) &&
                same(this.text, other.text);
    }

    public Object getValue(ValueMap taskInput, DecisionTask task) {
        var context = new DMNRootAPIObject(taskInput, task);
        return this.evaluator.evaluate(context);
    }
}
