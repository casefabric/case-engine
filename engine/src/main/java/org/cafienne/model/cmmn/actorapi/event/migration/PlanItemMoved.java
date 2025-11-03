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

package org.cafienne.model.cmmn.actorapi.event.migration;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.model.cmmn.actorapi.event.plan.PlanItemTransitioned;
import org.cafienne.model.cmmn.instance.PlanItem;
import org.cafienne.model.cmmn.instance.Stage;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;
import java.time.Instant;

@Manifest
public class PlanItemMoved extends PlanItemTransitioned {
    public final String newStageId;
    public final String planItemName;
    public final String definitionId;
    public final Instant createdOn;

    public PlanItemMoved(Stage<?> stage, PlanItem<?> item) {
        super(item, item.getState(), item.getHistoryState(), item.getLastTransition());
        this.newStageId = stage.getId();
        this.planItemName = item.getName();
        this.definitionId = item.getItemDefinition().getId();
        this.createdOn = item.getCaseInstance().getCreatedOn();
    }

    public PlanItemMoved(ValueMap json) {
        super(json);
        this.newStageId = json.readString(Fields.newStageId);
        this.planItemName = json.readString(Fields.name);
        this.definitionId = json.readString(Fields.definitionId, "");
        this.createdOn = getTimestamp();
    }

    @Override
    public String getDescription() {
        return "PlanItemMigrated [" + getType() + "-" + getPlanItemName() + "." + getIndex() + "/" + getPlanItemId() + "]";
    }

    public String getPlanItemName() {
        return planItemName;
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
        writeField(generator, Fields.newStageId, newStageId);
        writeField(generator, Fields.name, planItemName);
        writeField(generator, Fields.definitionId, definitionId);
    }

    @Override
    protected void updatePlanItemState(PlanItem<?> planItem) {
        planItem.updateState(this);
    }
}
