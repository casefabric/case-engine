/*
 * Copyright (C) 2014  Batav B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cafienne.model.cmmn.actorapi.event.plan.task.humantask;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.model.cmmn.instance.task.humantask.HumanTask;
import org.cafienne.model.cmmn.instance.task.humantask.TaskAction;
import org.cafienne.model.cmmn.instance.task.humantask.TaskState;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;

@Manifest
public class HumanTaskCompleted extends HumanTaskTransitioned {
    private final ValueMap taskOutput; // taskOutput - task saved output

    public HumanTaskCompleted(HumanTask task, ValueMap output) {
        super(task, TaskState.Completed, TaskAction.Complete);
        this.taskOutput = output;
    }

    public HumanTaskCompleted(ValueMap json) {
        super(json);
        this.taskOutput = json.readMap(Fields.taskOutput);
    }

    /**
     * Get assignee for the task
     * @return assignee for the task
     */
    public ValueMap getTaskOutput() {
        return this.taskOutput;
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.writeTransitionEvent(generator);
        writeField(generator, Fields.taskOutput, taskOutput);
    }
}
