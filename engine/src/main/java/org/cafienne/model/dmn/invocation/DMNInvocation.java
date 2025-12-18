package org.cafienne.model.dmn.invocation;

import org.cafienne.actormodel.debug.DebugInfoAppender;
import org.cafienne.model.cmmn.instance.task.decision.DecisionTask;
import org.cafienne.model.dmn.definition.DecisionModelDefinition;
import org.cafienne.model.processtask.definition.SubProcessDefinition;
import org.cafienne.model.processtask.definition.SubProcessOutputMappingDefinition;
import org.cafienne.util.json.Value;
import org.cafienne.util.json.ValueMap;

import java.util.Collection;

public class DMNInvocation  {
    public DMNInvocation(DecisionTask processTask, DecisionModelDefinition definition) {
        this.task = processTask;
        this.definition = definition;
    }

    protected final DecisionTask task;
    protected final DecisionModelDefinition definition;

    /**
     * This map contains a typically (fixed) set of variables representing the outcome of the http call. That is: responseCode, responseMessage, output and headers
     */
    private final ValueMap rawOutputParameters = new ValueMap();

    private final ValueMap processOutputParameters = new ValueMap();

    protected final void raiseComplete() {
        transformRawParametersToProcessOutputParameters(definition.getDecision().getSuccessMappings());
        task.goComplete(processOutputParameters);
    }

    protected final void raiseFault(String description) {
        transformRawParametersToProcessOutputParameters(definition.getDecision().getFailureMappings());
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
        rawOutputParameters.put(name, value);
        processOutputParameters.put(name, value);
    }

    /**
     * Invoked before reactive is invoked. Clears output parameters of earlier failures.
     */
    public void resetOutput() {
        rawOutputParameters.getValue().clear();
        processOutputParameters.getValue().clear();
    }

    protected void addDebugInfo(DebugInfoAppender appender) {
        task.getCaseInstance().addDebugInfo(appender);
    }

    protected void transformRawParametersToProcessOutputParameters(Collection<SubProcessOutputMappingDefinition> mappings) {
        addDebugInfo(() -> "Found " + mappings.size() +" output parameter mappings");
        // No support for raw output mappings currently
        rawOutputParameters.getValue().forEach((name, value) -> {
            processOutputParameters.put(name, value.cloneValueNode());
        });
    }

    /**
     * Sets a raw process parameter value; this may serve as input for a mapping
     * into the process output parameters.
     * @param name
     * @param value
     */
    protected void setRawOutputParameter(String name, Value<?> value) {
        rawOutputParameters.put(name, value);
    }

    /**
     * Directly set a process output parameter. Note that the value may be overriden by the mappings
     * that are invoked upon completion or failure of the process.
     * @param name
     * @param value
     */
    protected void setProcessOutputParameter(String name, Value<?> value) {
        processOutputParameters.put(name, value);
    }

    /**
     * Contains the map of raw parameter values of this sub process. Upon completion or failure of this instance, these parameters
     * will be mapped into the process output parameters if mappings are defined for them.
     * @return
     */
    protected ValueMap getRawOutputParameters() {
        return rawOutputParameters;
    }

    /**
     * Returns the map with the output parameters of the process, which will be used to map back into the ProcessTask parameters.
     * @return
     */
    protected ValueMap getProcessOutputParameters() {
        return processOutputParameters;
    }

    public void start() {

    }
}
