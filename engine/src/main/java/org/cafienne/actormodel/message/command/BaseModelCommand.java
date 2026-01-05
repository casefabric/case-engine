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

package org.cafienne.actormodel.message.command;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.apache.pekko.actor.ActorPath;
import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.exception.InvalidCommandException;
import org.cafienne.actormodel.identity.UserIdentity;
import org.cafienne.actormodel.message.response.ModelResponse;
import org.cafienne.infrastructure.serialization.Fields;
import org.cafienne.util.Guid;
import org.cafienne.util.json.JSONParseFailure;
import org.cafienne.util.json.JSONReader;
import org.cafienne.util.json.Value;
import org.cafienne.util.json.ValueMap;

import java.io.IOException;
import java.io.StringWriter;

public abstract class BaseModelCommand<T extends ModelActor, U extends UserIdentity> implements ModelCommand {
    protected final ValueMap json;
    public final String correlationId;
    public final String actorId;
    public final ActorMetadata target;
    protected transient T actor;
    private ModelResponse response;

    /**
     * Store the user that issued the Command.
     */
    private final U user;

    protected BaseModelCommand(U user, ActorMetadata target) {
        this.json = new ValueMap();
        this.target = validateMetadata(target);
        this.actorId = target.actorId();
        this.user = validateUser(user);
        this.correlationId = new Guid().toString();
    }

    private ActorMetadata validateMetadata(ActorMetadata metadata) {
        // First, make sure we have metadata
        if (metadata == null) {
            throw new InvalidCommandException("Target actor for a command of type " + this.getClass().getSimpleName() + " cannot be null");
        }
        // Next, validate actor id, also to be valid within in the actor system.
        //  Note: ActorPath.validate does not handle null pointers, therefore check on null happens first.
        if (metadata.actorId() == null) {
            throw new InvalidCommandException("Actor id cannot be null in command of type " + this.getClass().getSimpleName());
        }
        // Also let the actor system validate the actor id
        try {
            ActorPath.validatePathElement(metadata.actorId());
        } catch (Throwable t) {
            throw new InvalidCommandException("Invalid actor path " + metadata.actorId() + " in command of type " + this.getClass().getSimpleName(), t);
        }
        return metadata;
    }

    private U validateUser(U user) {
        if (user == null || user.id() == null || user.id().trim().isEmpty()) {
            throw new InvalidCommandException("User information is missing in command of type " + this.getClass().getSimpleName());
        }
        return user;
    }

    protected BaseModelCommand(ValueMap json) {
        this.json = json;
        if (json.has(Fields.metadata)) {
            ValueMap metadata = json.readMap(Fields.metadata);
            this.user = actorType().readUser(metadata.with(Fields.user));
            this.correlationId = metadata.readString(Fields.correlationId);
            this.target = metadata.readMetadata(Fields.target);
            this.actorId = metadata.readString(Fields.actorId);
        } else {
            // This is a command that is most probably wrapped in a ModelEvent of type CaseSystemCommunicationEvent, such as ActorRequestCreated and ActorRequestStored
//            System.out.println("Upgrading JSON deserialization of a " + getClass().getSimpleName() + " in a " + actorType());
            this.user = actorType().readUser(json.with(Fields.user));
            this.correlationId = json.readString(Fields.correlationId);
            this.actorId = json.readString(Fields.actorId);
            this.target = new ActorMetadata(this.actorType(), actorId, null);
        }
    }

    @Override
    public ActorMetadata target() {
        return target;
    }

    /**
     * Through this method, the command is made aware of the actor that is handling it.
     */
    @Override
    public void setActor(ModelActor actor) {
        this.actor = (T) actor;
    }

    @Override
    public final void validateCommand(ModelActor actor) {
        validate((T) actor);
    }

    @Override
    public final void processCommand(ModelActor actor) {
        process((T) actor);
    }

    @Override
    public ModelResponse getResponse() {
        return response;
    }

    public void setResponse(ModelResponse response) {
        this.response = response;
    }

    protected boolean hasNoResponse() {
        return response == null;
    }

    /**
     * Note: this method will only return a sensible value when it is invoked from within the command handling context.
     * It is intended for command handlers to have more metadata when creating a ModelResponse.
     */
    public T getActor() {
        return actor;
    }

    /**
     * Returns the user context for this command.
     */
    @Override
    public final U getUser() {
        return user;
    }

    /**
     * Returns a string with the identifier of the actor towards this command must be sent.
     */
    @Override
    public final String getActorId() {
        return actorId;
    }

    /**
     * Returns the correlation id of this command, that can be used to relate a {@link ModelResponse} back to this
     * original command.
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Before the Model Actor starts processing the command, it will first ask to validate the command.
     * Implementations may override this method to implement their own validation logic.
     * Implementations may throw the {@link InvalidCommandException} if they encounter a validation error
     *
     * @throws InvalidCommandException If the command is invalid
     */
    public abstract void validate(T modelActor) throws InvalidCommandException;

    /**
     * Method to be implemented to handle the command.
     */
    public abstract void process(T modelActor);

    @Override
    public void write(JsonGenerator generator) throws IOException {
        writeModelCommand(generator);
    }

    protected void writeModelCommand(JsonGenerator generator) throws IOException {
        generator.writeObjectFieldStart(Fields.metadata.toString());
        writeField(generator, Fields.type, this.getCommandDescription());
        writeField(generator, Fields.correlationId, this.getCorrelationId());
        writeField(generator, Fields.actorId, this.getActorId());
        writeField(generator, Fields.target, this.target);
        writeField(generator, Fields.user, user);
        generator.writeEndObject();
    }

    public ValueMap rawJson() {
        if (this.json.getValue().isEmpty()) {
            // We first need to serialize...
            JsonFactory factory = new JsonFactory();
            StringWriter sw = new StringWriter();
            try (JsonGenerator generator = factory.createGenerator(sw)) {
                generator.setPrettyPrinter(new DefaultPrettyPrinter());
                writeThisObject(generator);
                generator.close(); // need to close the generator, otherwise sw is empty
                return JSONReader.parse(sw.toString());
            } catch (IOException | JSONParseFailure e) {
                return new ValueMap("message", "Could not make JSON out of " + getClass().getName(), "exception", Value.convertThrowable(e));
            }
        }
        return this.json;
    }

    public String toString() {
        return "Command [" + getCommandDescription() + "]" + super.toString();
    }
}
