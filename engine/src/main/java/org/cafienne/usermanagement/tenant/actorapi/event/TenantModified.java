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

package org.cafienne.usermanagement.tenant.actorapi.event;

import org.cafienne.actormodel.identity.TenantUser;
import org.cafienne.actormodel.message.command.ModelCommand;
import org.cafienne.actormodel.message.event.ActorModified;
import org.cafienne.infrastructure.serialization.Manifest;
import org.cafienne.util.json.ValueMap;
import org.cafienne.usermanagement.tenant.TenantActor;

/**
 * Event that is published after an {@link org.cafienne.usermanagement.tenant.actorapi.command.TenantCommand} has been fully handled by a {@link TenantActor} instance.
 * Contains information about the last modified moment.
 *
 */
@Manifest
public class TenantModified extends ActorModified<TenantActor, TenantUser> implements TenantEvent {
    public TenantModified(TenantActor actor, ModelCommand source) {
        super(actor, source);
    }

    public TenantModified(ValueMap json) {
        super(json);
    }

}
