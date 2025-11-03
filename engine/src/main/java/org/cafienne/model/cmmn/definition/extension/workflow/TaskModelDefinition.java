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

package org.cafienne.model.cmmn.definition.extension.workflow;

import org.cafienne.model.cmmn.definition.CMMNElementDefinition;
import org.cafienne.model.cmmn.definition.ModelDefinition;
import org.cafienne.model.cmmn.definition.expression.ResolverDefinition;
import org.cafienne.model.cmmn.definition.parameter.InputParameterDefinition;
import org.cafienne.model.cmmn.expression.spel.api.cmmn.mapping.TaskInputRoot;
import org.cafienne.model.cmmn.instance.task.humantask.HumanTask;
import org.cafienne.util.json.JSONParseFailure;
import org.cafienne.util.json.JSONReader;
import org.cafienne.util.json.StringValue;
import org.cafienne.util.json.Value;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Map;

public class TaskModelDefinition extends ResolverDefinition {
    public TaskModelDefinition(Element element, ModelDefinition modelDefinition, CMMNElementDefinition parentElement) {
        super(element, modelDefinition, parentElement);
    }

    @Override
    public Map<String, InputParameterDefinition> getInputParameters() {
        return ((WorkflowTaskDefinition) getParentElement()).getInputParameters();
    }

    public Value<?> getValue(HumanTask task) {
        String taskModel = resolve(new TaskInputRoot(task));
        try {
            return JSONReader.parse(taskModel);
        } catch (IOException | JSONParseFailure e) {
            return new StringValue(taskModel);
        }
    }
}