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

package org.cafienne.model.dmn.actorapi.event;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.message.command.BootstrapMessage;
import org.cafienne.infrastructure.EngineVersion;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.model.dmn.actorapi.command.StartDecision;
import org.cafienne.model.dmn.actorapi.instance.DecisionTaskActor;
import org.cafienne.model.dmn.definition.DecisionModelDefinition;
import org.cafienne.model.processtask.definition.ProcessDefinition;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;

@Manifest
public class DecisionStarted extends BaseDecisionEvent implements BootstrapMessage {
    public final String parentActorId;
    public final String rootActorId;
    public final String name;
    public final ValueMap inputParameters;
    public transient DecisionModelDefinition definition;
    public final boolean debugMode;
    public final EngineVersion engineVersion;

    public DecisionStarted(DecisionTaskActor actor, StartDecision command) {
        super(actor);
        this.debugMode = command.debugMode();
        this.definition = command.getDefinition();
        this.name = command.getName();
        this.parentActorId = command.getParentActorId();
        this.rootActorId = command.getRootActorId();
        this.inputParameters = command.getInputParameters();
        this.engineVersion = actor.caseSystem.version();
    }

    public DecisionStarted(ValueMap json) {
        super(json);
        this.engineVersion = json.readObject(Fields.engineVersion, EngineVersion::new);
        this.name = json.readString(Fields.name);
        this.parentActorId = json.readString(Fields.parentActorId);
        this.rootActorId = json.readString(Fields.rootActorId);
        this.inputParameters = json.readMap(Fields.input);
        this.definition = json.readDefinition(Fields.processDefinition, ProcessDefinition.class);
        this.debugMode = json.readBoolean(Fields.debugMode);
    }

    @Override
    public void updateState(DecisionTaskActor actor) {
        actor.updateState(this);
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
        writeField(generator, Fields.input, inputParameters);
        writeField(generator, Fields.name, name);
        writeField(generator, Fields.parentActorId, parentActorId);
        writeField(generator, Fields.rootActorId, rootActorId);
        writeField(generator, Fields.debugMode, debugMode);
        writeField(generator, Fields.processDefinition, definition);
        writeField(generator, Fields.engineVersion, engineVersion.json());
    }
}
