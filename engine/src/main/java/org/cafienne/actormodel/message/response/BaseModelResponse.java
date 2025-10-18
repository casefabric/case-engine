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

package org.cafienne.actormodel.message.response;

import com.fasterxml.jackson.core.JsonGenerator;
import org.cafienne.actormodel.identity.UserIdentity;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;
import java.time.Instant;

/**
 * Basic implementation for ModelResponse to reply to a {@link ModelCommand}
 */
public abstract class BaseModelResponse implements ModelResponse {
    private final ValueMap json;
    private final String correlationId;
    private final String actorId;
    private Instant lastModified;
    private final UserIdentity user;
    private final String commandType;
    private final boolean actorChanged;

    protected BaseModelResponse(ModelCommand command) {
        this.json = new ValueMap();
        this.correlationId = command.getCorrelationId();
        this.actorId = command.actorId();
        // If a Command never reached the actor (e.g., if CaseSystem routing service ran into an error),
        //  the actor will not be available. Checking that here. Required for CommandFailure.
        this.lastModified = command.getActor() != null ? command.getActor().getLastModified() : null;
        this.actorChanged = command.getActor() != null && command.getActor().getCurrentTransaction() != null && command.getActor().getCurrentTransaction().hasState();
        this.user = command.getUser();
        this.commandType = command.getClass().getName();
    }

    protected BaseModelResponse(ValueMap json) {
        this.json = json;
        this.correlationId = json.readString(Fields.correlationId);
        this.actorId = json.readString(Fields.actorId);
        this.lastModified = json.readInstant(Fields.lastModified);
        this.user = actorType().readUser(json.with(Fields.user));
        this.commandType = json.readString(Fields.commandType);
        this.actorChanged = json.readBoolean(Fields.actorChanged);
    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns the actor to which a command was sent that led to this response
     * @return
     */
    public String getActorId() {
        return actorId;
    }

    /**
     * Returns the type of command that gave cause to this response
     *
     * @return
     */
    public String getCommandType() {
        return commandType;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    @Override
    public ActorLastModified lastModifiedContent() {
        return new ActorLastModified(actorId, getLastModified());
    }

    @Override
    public boolean actorChanged() {
        return actorChanged;
    }

    public UserIdentity getUser() {
        return user;
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        writeField(generator, Fields.correlationId, this.getCorrelationId());
        writeField(generator, Fields.actorId, actorId);
        writeField(generator, Fields.commandType, commandType);
        writeField(generator, Fields.lastModified, this.getLastModified());
        writeField(generator, Fields.actorChanged, actorChanged);
        writeField(generator, Fields.user, user);
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public ValueMap rawJson() {
        return json;
    }
}
