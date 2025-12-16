package org.cafienne.model.dmn.operation;

import org.cafienne.model.cmmn.instance.task.process.ProcessTask;
import org.cafienne.model.dmn.definition.DecisionDefinition;
import org.cafienne.model.processtask.implementation.InlineSubProcess;

public class DMNInvocation extends InlineSubProcess<DecisionDefinition> {
    public DMNInvocation(ProcessTask processTask, DecisionDefinition definition) {
        super(processTask, definition);
    }

    @Override
    public void start() {

    }

    @Override
    public void reactivate() {

    }

    @Override
    public void suspend() {

    }

    @Override
    public void terminate() {

    }

    @Override
    public void resume() {

    }
}
