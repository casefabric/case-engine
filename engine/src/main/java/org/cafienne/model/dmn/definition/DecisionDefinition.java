package org.cafienne.model.dmn.definition;

import org.cafienne.model.cmmn.definition.CMMNElementDefinition;
import org.cafienne.model.cmmn.definition.ModelDefinition;
import org.cafienne.model.cmmn.instance.task.process.ProcessTask;
import org.cafienne.model.processtask.definition.InlineSubProcessDefinition;
import org.cafienne.model.processtask.implementation.InlineSubProcess;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DecisionDefinition extends InlineSubProcessDefinition {
    private final List<DecisionTableDefinition> decisionTables = new ArrayList<>();
    private final List<ParameterDefinition> inputs = new ArrayList<>();
    private final List<ParameterDefinition> outputs = new ArrayList<>();


    public DecisionDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement);

        parse("decisionTable", DecisionTableDefinition.class, decisionTables);
        parse("input", ParameterDefinition.class, inputs);
        parse("output", ParameterDefinition.class, outputs);
    }

    @Override
    protected Set<String> getRawOutputParameterNames() {
        return Set.of();
    }

    @Override
    public InlineSubProcess<?> createInstance(ProcessTask task) {
        return null;
    }

    @Override
    public boolean equalsWith(Object object) {
        return equalsWith(object, this::sameTable);
    }

    public boolean sameTable(DecisionDefinition other) {
        return sameIdentifiers(other) &&
                same(this.decisionTables, other.decisionTables) &&
                same(this.inputs, other.inputs) &&
                same(this.outputs, other.outputs);

    }
}
