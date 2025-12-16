package org.cafienne.model.dmn.definition;

import org.cafienne.model.cmmn.expression.spel.api.CaseRootObject;
import org.cafienne.model.cmmn.instance.task.dmn.DecisionTask;
import org.cafienne.util.json.ValueMap;

public class DMNRootAPIObject extends CaseRootObject {
    private final DecisionTask task;

    public DMNRootAPIObject(ValueMap taskInput, DecisionTask task) {
        super(task.getCaseInstance());
        this.task = task;

        // Add a reader for each input
        taskInput.getValue().forEach((input, value) -> addPropertyReader(input, () -> value));

    }

    @Override
    public String getDescription() {
        return "Decision root API Object";
    }}
