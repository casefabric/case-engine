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

package org.cafienne.model.dmn.actorapi.command;

import org.cafienne.actormodel.identity.CaseUserIdentity;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.model.dmn.actorapi.event.DecisionSuspended;
import org.cafienne.model.dmn.actorapi.instance.DecisionTaskActor;
import org.cafienne.model.dmn.invocation.DMNInvocation;
import org.cafienne.util.json.ValueMap;

@Manifest
public class SuspendDecision extends DecisionCommand {
    public SuspendDecision(CaseUserIdentity user, String id) {
        super(user, id);
    }

    public SuspendDecision(ValueMap json) {
        super(json);
    }

    @Override
    protected void process(DecisionTaskActor processTaskActor, DMNInvocation implementation) {
        processTaskActor.addEvent(new DecisionSuspended(processTaskActor));
    }
}
