package org.cafienne.model.dmn.definition;

import org.cafienne.model.cmmn.definition.DefinitionsDocument;
import org.cafienne.model.cmmn.definition.ModelDefinition;
import org.cafienne.model.cmmn.definition.task.TaskImplementationContract;
import org.cafienne.model.cmmn.instance.task.dmn.DecisionTask;
import org.cafienne.model.dmn.invocation.DMNInvocation;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class DecisionModelDefinition  extends ModelDefinition
    implements TaskImplementationContract {

    private final List<DecisionTableDefinition> decisionTables = new ArrayList<>();
    private final List<ParameterDefinition> inputs = new ArrayList<>();
    private final List<ParameterDefinition> outputs = new ArrayList<>();


    public DecisionModelDefinition(Element definitionElement, DefinitionsDocument document) {
        super(definitionElement, document);

        parse("decisionTable", DecisionTableDefinition.class, decisionTables);
        parse("input", ParameterDefinition.class, inputs);
        parse("output", ParameterDefinition.class, outputs);
   }

    public List<ParameterDefinition> getOutputs() {
        return outputs;
    }

    public List<ParameterDefinition> getInputs() {
        return inputs;
    }

    @Override
    public boolean equalsWith(Object object) {
        return equalsWith(object, this::sameDecisionDefinition);
    }

    public boolean sameDecisionDefinition(DecisionModelDefinition other) {
        return sameModelDefinition(other)  &&
                same(this.decisionTables, other.decisionTables) &&
                same(this.inputs, other.inputs) &&
                same(this.outputs, other.outputs);
    }

    public DMNInvocation createInstance(DecisionTask task) {
        return new DMNInvocation(task, this);
    }
}
