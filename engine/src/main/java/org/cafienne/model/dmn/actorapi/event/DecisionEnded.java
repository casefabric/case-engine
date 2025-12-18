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
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.model.dmn.actorapi.instance.DecisionTaskActor;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;

public abstract class DecisionEnded extends BaseDecisionEvent {

    public final ValueMap output;

    protected DecisionEnded(DecisionTaskActor actor, ValueMap outputParameters) {
        super(actor);
        this.output = outputParameters;
    }

    protected DecisionEnded(ValueMap json) {
        super(json);
        this.output = json.readMap(Fields.output);
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
        writeField(generator, Fields.output, output);
    }
}
