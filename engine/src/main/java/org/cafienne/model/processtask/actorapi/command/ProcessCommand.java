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

package org.cafienne.model.processtask.actorapi.command;

import org.cafienne.actormodel.exception.InvalidCommandException;
import org.cafienne.actormodel.identity.CaseUserIdentity;
import org.cafienne.actormodel.message.command.BaseModelCommand;
import org.cafienne.model.processtask.actorapi.ProcessActorMessage;
import org.cafienne.model.processtask.actorapi.response.ProcessResponse;
import org.cafienne.model.processtask.implementation.SubProcess;
import org.cafienne.model.processtask.instance.ProcessTaskActor;
import org.cafienne.util.json.ValueMap;

public abstract class ProcessCommand extends BaseModelCommand<ProcessTaskActor, CaseUserIdentity> implements ProcessActorMessage {
    protected ProcessCommand(CaseUserIdentity user, String id) {
        super(user, id);
    }

    protected ProcessCommand(ValueMap json) {
        super(json);
    }

    @Override
    public void validate(ProcessTaskActor modelActor) throws InvalidCommandException {
        // Nothing to validate
    }

    @Override
    public void process(ProcessTaskActor processTaskActor) {
        process(processTaskActor, processTaskActor.getImplementation());
        if (hasNoResponse()) { // Always return a response
            setResponse(new ProcessResponse(this));
        }
    }

    abstract protected void process(ProcessTaskActor processTaskActor, SubProcess<?> implementation);
}
