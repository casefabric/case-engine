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

package org.cafienne.actormodel.message.event;

import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.message.UserMessage;

import java.time.Instant;
import java.util.Set;

public interface ModelEvent extends UserMessage {
    String TAG = "cafienne";

    Set<String> tags = Set.of(ModelEvent.TAG);

    default Set<String> tags() {
        return tags;
    }

    ActorMetadata metadata();

    /**
     * Returns the identifier of the outer-most parent that started this model actor. E.g., if this event happened in a subcase,
     * then this will return the id of it's top most ancestor case starting it, and for which the subcase is a blocking task.
     * So, non-blocking subcase tasks are stand alone, and will not return the identifier of the parent case that started it.
     * Furthermore, if this case is itself a root-case, then root case id and case instance id will be the same.
     */
    default String rootActorId() {
        return metadata().root().actorId();
    }

    /**
     * Returns the identifier of the ModelActor that started the subcase causing this event to happen, or null if there was no parent.
     * Note, this will also return the parent case id if this subcase was started in non-blocking mode (as opposed to getRootCaseId behavior)
     *
     */
    default String parentActorId() {
        return metadata().parentId();
    }

    /**
     * Hook that will be invoked after the event is persisted.
     * This can be used to run followup actions only after transaction completed.
     */
    default void afterPersist(ModelActor actor) {
    }

    void updateActorState(ModelActor actor);

    String tenant();

    Instant getTimestamp();
}
