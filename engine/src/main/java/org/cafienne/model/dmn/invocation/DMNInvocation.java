package org.cafienne.model.dmn.invocation;

import org.cafienne.actormodel.debug.DebugInfoAppender;
import org.cafienne.model.cmmn.instance.task.dmn.DecisionTask;
import org.cafienne.model.dmn.definition.DecisionModelDefinition;
import org.cafienne.model.dmn.definition.DecisionRuleDefinition;
import org.cafienne.model.processtask.definition.SubProcessDefinition;
import org.cafienne.util.json.Value;
import org.cafienne.util.json.ValueMap;

public class DMNInvocation  {
    public DMNInvocation(DecisionTask processTask, DecisionModelDefinition definition) {
        this.task = processTask;
        this.definition = definition;
    }

    protected final DecisionTask task;
    protected final DecisionModelDefinition definition;

    private final ValueMap processOutputParameters = new ValueMap();

    protected final void raiseComplete() {
        task.goComplete(processOutputParameters);
    }

    protected final void raiseFault(String description) {
        task.goFault(processOutputParameters);
    }

    /**
     * Store an exception in the process output parameters (and in the raw parameters),
     * under the name {@link SubProcessDefinition#EXCEPTION_PARAMETER}, and raise fault for the task.
     * @param cause
     */
    protected void raiseFault(String message, Throwable cause) {
        setFault(Value.convert(cause));
        raiseFault(message);
    }

    /**
     * Stores the value as an output parameter both in the raw and in the process output parameters.
     * This enables propagating faults back into the process task without any in-process mappings defined.
     * @param value
     */
    protected void setFault(Value<?> value) {
        String name = SubProcessDefinition.EXCEPTION_PARAMETER;
        processOutputParameters.put(name, value);
    }

    /**
     * Invoked before reactive is invoked. Clears output parameters of earlier failures.
     */
    public void resetOutput() {
        processOutputParameters.getValue().clear();
    }

    protected void addDebugInfo(DebugInfoAppender appender) {
        task.getCaseInstance().addDebugInfo(appender);
    }

    private void setProcessOutputParameter(String name, Value<?> value) {
        processOutputParameters.put(name, value);
    }

    public void start() {
        resetOutput();

        var table = definition.getTable();
        var rules = table.getRule();
        ValueMap taskInput = task.getMappedInputParameters();
        var eligibleRules = rules.stream().filter(rule -> rule.isApplicable(taskInput, task)).toList();

        DecisionRuleDefinition selectedRule;
        switch (table.getHitPolicy()) {
            case ANY,UNIQUE,FIRST ->  selectedRule = eligibleRules.getFirst();
            default -> selectedRule = eligibleRules.getFirst();
        }

        var result = selectedRule.getOutputs(taskInput, task);

        var outputs = definition.getOutputs();
        for (int i=0; i<outputs.size(); i++) {
            var output = outputs.get(i);
            setProcessOutputParameter(output.getName(), Value.convert(result[i]));
        }

        raiseComplete();
    }
}
