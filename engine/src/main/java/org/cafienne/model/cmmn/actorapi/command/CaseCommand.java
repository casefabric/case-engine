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

package org.cafienne.model.cmmn.actorapi.command;

import org.cafienne.actormodel.ActorMetadata;
import org.cafienne.actormodel.ActorType;
import org.cafienne.actormodel.exception.InvalidCommandException;
import org.cafienne.actormodel.identity.CaseUserIdentity;
import org.cafienne.actormodel.message.command.BaseModelCommand;
import org.cafienne.model.cmmn.actorapi.CaseMessage;
import org.cafienne.model.cmmn.actorapi.command.plan.MakePlanItemTransition;
import org.cafienne.model.cmmn.actorapi.event.CaseEvent;
import org.cafienne.model.cmmn.actorapi.response.CaseResponse;
import org.cafienne.model.cmmn.instance.Case;
import org.cafienne.util.json.ValueMap;

/**
 * A {@link Case} instance is designed to handle various CaseCommands, such as {@link StartCase}, {@link MakePlanItemTransition}, etc.
 * Each CaseCommand must implement its own logic within the case, through the optional {@link BaseModelCommand#validate} and the mandatory {@link CaseCommand#process} methods.
 * When the case has successfully handled the command, it will persist the resulting {@link CaseEvent}s, and send a reply back, see {@link CaseResponse}.
 */
public abstract class CaseCommand extends BaseModelCommand<Case, CaseUserIdentity> implements CaseMessage {
    /**
     * Create a new command that can be sent to the case.
     *
     * @param user           The user that issues this command.
     * @param caseInstanceId The id of the case in which to perform this command.
     */
    protected CaseCommand(CaseUserIdentity user, String caseInstanceId) {
        super(user, new ActorMetadata(ActorType.Case, caseInstanceId, null));
    }

    protected CaseCommand(ValueMap json) {
        super(json);
    }

    /**
     * The id of the case on which to perform the command
     *
     */
    @Deprecated
    public String getCaseInstanceId() {
        return actorId;
    }

    /**
     * Before the case starts processing the command, it will first ask to validate the command.
     * The default implementation is to check whether the case definition is available (i.e., whether StartCase command has been triggered before this command).
     * Implementations can override this method to implement their own validation logic.
     * Implementations may throw the {@link InvalidCommandException} if they encounter a validation error
     *
     * @throws InvalidCommandException If the command is invalid
     */
    public void validate(Case caseInstance) throws InvalidCommandException {
        // Validate case team membership
        validateCaseTeamMembership(caseInstance);
    }

    /**
     * This method validates the case team membership of the tenant user that sent this command
     */
    protected void validateCaseTeamMembership(Case caseInstance) {
        caseInstance.getCaseTeam().validateMembership(getUser());
    }

    @Override
    public void process(Case caseInstance) {
        processCaseCommand(caseInstance);
        if (hasNoResponse()) { // Always return a response
            setResponse(new CaseResponse(this));
        }
    }

    public abstract void processCaseCommand(Case caseInstance);
}
