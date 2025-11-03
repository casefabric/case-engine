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

package org.cafienne.usermanagement.tenant.serializers;

import org.cafienne.infrastructure.serialization.CafienneSerializer;
import org.cafienne.usermanagement.tenant.actorapi.command.GetTenantOwners;
import org.cafienne.usermanagement.tenant.actorapi.command.RemoveTenantUser;
import org.cafienne.usermanagement.tenant.actorapi.command.ReplaceTenant;
import org.cafienne.usermanagement.tenant.actorapi.command.SetTenantUser;
import org.cafienne.usermanagement.tenant.actorapi.command.platform.CreateTenant;
import org.cafienne.usermanagement.tenant.actorapi.command.platform.DisableTenant;
import org.cafienne.usermanagement.tenant.actorapi.command.platform.EnableTenant;
import org.cafienne.usermanagement.tenant.actorapi.event.TenantAppliedPlatformUpdate;
import org.cafienne.usermanagement.tenant.actorapi.event.TenantModified;
import org.cafienne.usermanagement.tenant.actorapi.event.deprecated.*;
import org.cafienne.usermanagement.tenant.actorapi.event.platform.TenantCreated;
import org.cafienne.usermanagement.tenant.actorapi.event.platform.TenantDisabled;
import org.cafienne.usermanagement.tenant.actorapi.event.platform.TenantEnabled;
import org.cafienne.usermanagement.tenant.actorapi.event.user.TenantUserAdded;
import org.cafienne.usermanagement.tenant.actorapi.event.user.TenantUserChanged;
import org.cafienne.usermanagement.tenant.actorapi.event.user.TenantUserRemoved;
import org.cafienne.usermanagement.tenant.actorapi.response.TenantOwnersResponse;
import org.cafienne.usermanagement.tenant.actorapi.response.TenantResponse;

public class TenantSerializers {
    public static void register() {
        registerTenantCommands();
        registerTenantEvents();
        registerTenantResponses();
        registerPlatformCommands();
        registerPlatformEvents();
    }

    private static void registerTenantCommands() {
        CafienneSerializer.addManifestWrapper(SetTenantUser.class, SetTenantUser::new);
        CafienneSerializer.addManifestWrapper(RemoveTenantUser.class, RemoveTenantUser::new);
        CafienneSerializer.addManifestWrapper(GetTenantOwners.class, GetTenantOwners::new);
        CafienneSerializer.addManifestWrapper(ReplaceTenant.class, ReplaceTenant::new);
    }

    private static void registerPlatformCommands() {
        CafienneSerializer.addManifestWrapper(CreateTenant.class, CreateTenant::new);
        CafienneSerializer.addManifestWrapper(DisableTenant.class, DisableTenant::new);
        CafienneSerializer.addManifestWrapper(EnableTenant.class, EnableTenant::new);
    }

    private static void registerTenantEvents() {
        CafienneSerializer.addManifestWrapper(TenantOwnersRequested.class, TenantOwnersRequested::new);
        CafienneSerializer.addManifestWrapper(TenantModified.class, TenantModified::new);
        CafienneSerializer.addManifestWrapper(TenantAppliedPlatformUpdate.class, TenantAppliedPlatformUpdate::new);
        CafienneSerializer.addManifestWrapper(TenantUserAdded.class, TenantUserAdded::new);
        CafienneSerializer.addManifestWrapper(TenantUserChanged.class, TenantUserChanged::new);
        CafienneSerializer.addManifestWrapper(TenantUserRemoved.class, TenantUserRemoved::new);
        registerDeprecatedTenantEvents();
    }

    private static void registerDeprecatedTenantEvents() {
        CafienneSerializer.addManifestWrapper(TenantUserCreated.class, TenantUserCreated::new);
        CafienneSerializer.addManifestWrapper(TenantUserUpdated.class, TenantUserUpdated::new);
        CafienneSerializer.addManifestWrapper(TenantUserRoleAdded.class, TenantUserRoleAdded::new);
        CafienneSerializer.addManifestWrapper(TenantUserRoleRemoved.class, TenantUserRoleRemoved::new);
        CafienneSerializer.addManifestWrapper(TenantUserEnabled.class, TenantUserEnabled::new);
        CafienneSerializer.addManifestWrapper(TenantUserDisabled.class, TenantUserDisabled::new);
        CafienneSerializer.addManifestWrapper(OwnerAdded.class, OwnerAdded::new);
        CafienneSerializer.addManifestWrapper(OwnerRemoved.class, OwnerRemoved::new);
    }

    private static void registerPlatformEvents() {
        CafienneSerializer.addManifestWrapper(TenantCreated.class, TenantCreated::new);
        CafienneSerializer.addManifestWrapper(TenantDisabled.class, TenantDisabled::new);
        CafienneSerializer.addManifestWrapper(TenantEnabled.class, TenantEnabled::new);
    }

    private static void registerTenantResponses() {
        CafienneSerializer.addManifestWrapper(TenantOwnersResponse.class, TenantOwnersResponse::new);
        CafienneSerializer.addManifestWrapper(TenantResponse.class, TenantResponse::new);
    }
}
