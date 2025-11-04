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

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.identity.UserIdentity;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;
import java.time.Instant;

public abstract class BaseModelEvent<M extends ModelActor, U extends UserIdentity> implements ModelEvent {
    private final ValueMap json;
    
    // Serializable fields
    public final ActorMetadata actor;
    private final String actorId;
    public final String tenant;
    private final U user;
    private final Instant timestamp;
    private final String correlationId;

    protected BaseModelEvent(M actor) {
        this.json = new ValueMap();
        this.actor = actor.metadata();
        this.actorId = actor.getId();
        this.tenant = actor.getTenant();
        this.timestamp = actor.getTransactionTimestamp();
        // During recovery, events update the actor state, which in itself can lead to new events.
        // These events are not added if recovery is running.
        //  Since we're recovering, there is no current transaction.
        //  In that case we set user and correlation id to null (since the event is ignored anyway).
        this.user = (U) actor.getCurrentUser();
        this.correlationId =  actor.getCurrentTransaction().getCorrelationId();
    }

    protected BaseModelEvent(ValueMap json) {
        this.json = json;
        ValueMap modelEventJson = json.with(Fields.modelEvent);
        this.actor = modelEventJson.readMetadata(Fields.actor);
        this.actorId = modelEventJson.readString(Fields.actorId);
        this.tenant = modelEventJson.readString(Fields.tenant);
        this.timestamp = modelEventJson.readInstant(Fields.timestamp);
        this.user = actorType().readUser(modelEventJson.with(Fields.user));
        this.correlationId = modelEventJson.readString(Fields.correlationId);
    }

    @Override
    public ActorMetadata metadata() {
        return actor;
    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public String tenant() {
        return tenant;
    }

    /**
     * Returns the raw json used to (de)serialize this event
     * This method cannot be invoked upon first event creation.
     */
    public final ValueMap rawJson() {
        return this.json;
    }

    /**
     * Returns the identifier of the ModelActor that generated this event.
     * Is the same as the persistence id of the underlying Actor.
     */
    public final String getActorId() {
        return this.actorId;
    }

    /**
     * Returns the complete context of the user that caused the event to happen
     */
    public final UserIdentity getUser() {
        return user;
    }

    /**
     * Returns the event timestamp
     */
    public final Instant getTimestamp() {
        return timestamp;
    }

    /**
     * UpdateState will be invoked when an event is added or recovered.
     */
    public abstract void updateState(M actor);

    @Override
    public final void updateActorState(ModelActor actor) {
        // A very hard cast indeed. But it would be weird if it doesn't work...
        updateState((M) actor);
    }

    protected void writeModelEvent(JsonGenerator generator) throws IOException {
        generator.writeFieldName(Fields.modelEvent.toString());
        generator.writeStartObject();
        writeField(generator, Fields.actor, this.actor);
        writeField(generator, Fields.actorId, this.getActorId());
        writeField(generator, Fields.correlationId, this.getCorrelationId());
        writeField(generator, Fields.tenant, this.tenant);
        writeField(generator, Fields.timestamp, this.timestamp);
        writeField(generator, Fields.user, user);
        generator.writeEndObject();
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
