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

package org.cafienne.usermanagement.consentgroup.serializers;

import org.cafienne.infrastructure.serialization.CafienneSerializer;
import org.cafienne.usermanagement.consentgroup.actorapi.command.CreateConsentGroup;
import org.cafienne.usermanagement.consentgroup.actorapi.command.RemoveConsentGroupMember;
import org.cafienne.usermanagement.consentgroup.actorapi.command.ReplaceConsentGroup;
import org.cafienne.usermanagement.consentgroup.actorapi.command.SetConsentGroupMember;
import org.cafienne.usermanagement.consentgroup.actorapi.event.*;
import org.cafienne.usermanagement.consentgroup.actorapi.response.ConsentGroupCreatedResponse;
import org.cafienne.usermanagement.consentgroup.actorapi.response.ConsentGroupResponse;

public class ConsentGroupSerializers {
    public static void register() {
        registerConsentGroupCommands();
        registerConsentGroupEvents();
        registerConsentGroupResponses();
    }

    private static void registerConsentGroupCommands() {
        CafienneSerializer.addManifestWrapper(CreateConsentGroup.class, CreateConsentGroup::new);
        CafienneSerializer.addManifestWrapper(ReplaceConsentGroup.class, ReplaceConsentGroup::new);
        CafienneSerializer.addManifestWrapper(SetConsentGroupMember.class, SetConsentGroupMember::new);
        CafienneSerializer.addManifestWrapper(RemoveConsentGroupMember.class, RemoveConsentGroupMember::new);
    }

    private static void registerConsentGroupEvents() {
        CafienneSerializer.addManifestWrapper(ConsentGroupMemberAdded.class, ConsentGroupMemberAdded::new);
        CafienneSerializer.addManifestWrapper(ConsentGroupMemberChanged.class, ConsentGroupMemberChanged::new);
        CafienneSerializer.addManifestWrapper(ConsentGroupMemberRemoved.class, ConsentGroupMemberRemoved::new);
        CafienneSerializer.addManifestWrapper(ConsentGroupCreated.class, ConsentGroupCreated::new);
        CafienneSerializer.addManifestWrapper(ConsentGroupModified.class, ConsentGroupModified::new);
    }

    private static void registerConsentGroupResponses() {
        CafienneSerializer.addManifestWrapper(ConsentGroupCreatedResponse.class, ConsentGroupCreatedResponse::new);
        CafienneSerializer.addManifestWrapper(ConsentGroupResponse.class, ConsentGroupResponse::new);
    }
}
