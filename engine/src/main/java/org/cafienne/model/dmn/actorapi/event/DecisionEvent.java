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

import org.cafienne.actormodel.message.event.ModelEvent;
import org.cafienne.model.dmn.actorapi.DecisionActorMessage;

import java.util.Set;

public interface DecisionEvent extends ModelEvent, DecisionActorMessage {
    String TAG = "cafienne:decision";

    Set<String> tags = Set.of(ModelEvent.TAG, DecisionEvent.TAG);

    @Override
    default Set<String> tags() {
        return tags;
    }
}
