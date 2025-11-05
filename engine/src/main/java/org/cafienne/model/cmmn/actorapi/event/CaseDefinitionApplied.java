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

package org.cafienne.model.cmmn.actorapi.event;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ActorType;
import org.cafienne.actormodel.message.command.BootstrapMessage;
import org.cafienne.infrastructure.EngineVersion;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.model.cmmn.actorapi.event.definition.CaseDefinitionEvent;
import org.cafienne.model.cmmn.definition.CaseDefinition;
import org.cafienne.model.cmmn.instance.Case;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;
import java.time.Instant;

@Manifest
public class CaseDefinitionApplied extends CaseDefinitionEvent implements BootstrapMessage {
    public final EngineVersion engineVersion;
    public final Instant createdOn;
    public final String createdBy;

    public CaseDefinitionApplied(Case caseInstance, CaseDefinition definition) {
        super(caseInstance, definition);
        this.createdOn = caseInstance.getTransactionTimestamp();
        this.createdBy = caseInstance.getCurrentUser().id();
        // Whenever a new case is started, a case definition is applied.
        //  So, at that moment we also store the engine version.
        //  TODO: perhaps better to distinguish CaseStarted or CaseCreated from CaseDefinitionApplied
        //   If so, then we can also suffice with storing root id and so in the CaseCreated, rather than in case definition applied. Same for engine version.
        this.engineVersion = caseInstance.caseSystem.version();
    }

    public CaseDefinitionApplied(ValueMap json) {
        super(json);
        this.createdOn = json.readInstant(Fields.createdOn);
        this.createdBy = json.readString(Fields.createdBy);
        this.engineVersion = json.readObject(Fields.engineVersion, EngineVersion::new);
    }

    @Override
    protected ActorMetadata createActorMetadata(ValueMap json) {
        String rootCaseId = json.readString(Fields.rootActorId);
        String parentCaseId = json.readString(Fields.parentActorId);
        ActorMetadata root = new ActorMetadata(ActorType.Case, rootCaseId, null);
        ActorMetadata parent = new ActorMetadata(ActorType.Case, parentCaseId, root);
        // Not full chain is known here, but this is ok enough for now.
        return new ActorMetadata(ActorType.Case, actorId(), parent);
    }

    @Override
    public String toString() {
        return "Case definition " + getCaseName();
    }

    public void updateState(Case caseInstance) {
        caseInstance.updateState(this);
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.writeCaseDefinitionEvent(generator);
        writeField(generator, Fields.createdOn, createdOn);
        writeField(generator, Fields.createdBy, createdBy);
        // Keep writing root and parent for compatibility
        writeField(generator, Fields.rootActorId, rootActorId());
        writeField(generator, Fields.parentActorId, parentActorId());
        writeField(generator, Fields.engineVersion, engineVersion.json());
    }
}
