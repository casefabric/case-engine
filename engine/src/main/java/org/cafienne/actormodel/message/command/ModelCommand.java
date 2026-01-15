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

import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ModelActor;
import org.cafienne.actormodel.message.UserMessage;
import org.cafienne.actormodel.message.response.ModelResponse;

public interface ModelCommand extends UserMessage {
    /**
     * Return the actor handling this command. May return null if setActor() is not yet invoked.
     */
    ModelActor getActor();

    /**
     * Returns the identifier of the ModelActor to which this command is sent
     */
    ActorMetadata target();

    /**
     * Enable setting the correlation id for this command from an outside source.
     * Note: this may throw an exception if the method is invoked during or after command handling.
     */
    void setCorrelationId(String correlationId);

    /**
     * Through this method, the command is made aware of the actor that is handling it.
     */
    void setActor(ModelActor actor);

    void validateCommand(ModelActor actor);

    void processCommand(ModelActor actor);

    ModelResponse getResponse();

    default String getCommandDescription() {
        return getDescription();
    }
}
