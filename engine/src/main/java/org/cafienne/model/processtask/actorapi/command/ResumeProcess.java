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

import org.cafienne.actormodel.identity.CaseUserIdentity;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.model.processtask.actorapi.event.ProcessResumed;
import org.cafienne.model.processtask.implementation.SubProcess;
import org.cafienne.model.processtask.instance.ProcessTaskActor;
import org.cafienne.util.json.ValueMap;

@Manifest
public class ResumeProcess extends ProcessCommand {
    public ResumeProcess(CaseUserIdentity user, String id) {
        super(user, id);
    }

    public ResumeProcess(ValueMap json) {
        super(json);
    }

    @Override
    protected void process(ProcessTaskActor processTaskActor, SubProcess<?> implementation) {
        processTaskActor.addEvent(new ProcessResumed(processTaskActor));
    }
}
