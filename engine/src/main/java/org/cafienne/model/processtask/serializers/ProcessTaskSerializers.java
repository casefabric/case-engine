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

package org.cafienne.model.processtask.serializers;

import org.cafienne.infrastructure.serialization.CafienneSerializer;
import org.cafienne.model.processtask.actorapi.command.*;
import org.cafienne.model.processtask.actorapi.event.*;
import org.cafienne.model.processtask.actorapi.response.ProcessResponse;

public class ProcessTaskSerializers {
    public static void register() {
        registerProcessActorCommands();
        registerProcessEvents();
        registerProcessResponses();
    }

    private static void registerProcessActorCommands() {
        CafienneSerializer.addManifestWrapper(StartProcess.class, StartProcess::new);
        CafienneSerializer.addManifestWrapper(ResumeProcess.class, ResumeProcess::new);
        CafienneSerializer.addManifestWrapper(ReactivateProcess.class, ReactivateProcess::new);
        CafienneSerializer.addManifestWrapper(SuspendProcess.class, SuspendProcess::new);
        CafienneSerializer.addManifestWrapper(TerminateProcess.class, TerminateProcess::new);
        CafienneSerializer.addManifestWrapper(MigrateProcessDefinition.class, MigrateProcessDefinition::new);
    }

    private static void registerProcessEvents() {
        CafienneSerializer.addManifestWrapper(ProcessStarted.class, ProcessStarted::new);
        CafienneSerializer.addManifestWrapper(ProcessCompleted.class, ProcessCompleted::new);
        CafienneSerializer.addManifestWrapper(ProcessFailed.class, ProcessFailed::new);
        CafienneSerializer.addManifestWrapper(ProcessReactivated.class, ProcessReactivated::new);
        CafienneSerializer.addManifestWrapper(ProcessResumed.class, ProcessResumed::new);
        CafienneSerializer.addManifestWrapper(ProcessSuspended.class, ProcessSuspended::new);
        CafienneSerializer.addManifestWrapper(ProcessTerminated.class, ProcessTerminated::new);
        CafienneSerializer.addManifestWrapper(ProcessModified.class, ProcessModified::new);
        CafienneSerializer.addManifestWrapper(ProcessDefinitionMigrated.class, ProcessDefinitionMigrated::new);
    }

    private static void registerProcessResponses() {
        CafienneSerializer.addManifestWrapper(ProcessResponse.class, ProcessResponse::new);
    }
}
