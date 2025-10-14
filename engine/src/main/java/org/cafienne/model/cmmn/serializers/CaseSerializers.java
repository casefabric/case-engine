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

package org.cafienne.model.cmmn.serializers;

import org.cafienne.actormodel.message.event.EngineVersionChanged;
import org.cafienne.infrastructure.serialization.CafienneSerializer;
import org.cafienne.model.cmmn.actorapi.command.ReactivateCase;
import org.cafienne.model.cmmn.actorapi.command.StartCase;
import org.cafienne.model.cmmn.actorapi.command.casefile.CreateCaseFileItem;
import org.cafienne.model.cmmn.actorapi.command.casefile.DeleteCaseFileItem;
import org.cafienne.model.cmmn.actorapi.command.casefile.ReplaceCaseFileItem;
import org.cafienne.model.cmmn.actorapi.command.casefile.UpdateCaseFileItem;
import org.cafienne.model.cmmn.actorapi.command.debug.SwitchDebugMode;
import org.cafienne.model.cmmn.actorapi.command.migration.MigrateCaseDefinition;
import org.cafienne.model.cmmn.actorapi.command.migration.MigrateDefinition;
import org.cafienne.model.cmmn.actorapi.command.plan.AddDiscretionaryItem;
import org.cafienne.model.cmmn.actorapi.command.plan.GetDiscretionaryItems;
import org.cafienne.model.cmmn.actorapi.command.plan.MakeCaseTransition;
import org.cafienne.model.cmmn.actorapi.command.plan.MakePlanItemTransition;
import org.cafienne.model.cmmn.actorapi.command.plan.eventlistener.RaiseEvent;
import org.cafienne.model.cmmn.actorapi.command.plan.task.CompleteTask;
import org.cafienne.model.cmmn.actorapi.command.plan.task.FailTask;
import org.cafienne.model.cmmn.actorapi.command.plan.task.HandleTaskImplementationTransition;
import org.cafienne.model.cmmn.actorapi.command.plan.task.humantask.*;
import org.cafienne.model.cmmn.actorapi.command.team.DeprecatedUpsert;
import org.cafienne.model.cmmn.actorapi.command.team.SetCaseTeam;
import org.cafienne.model.cmmn.actorapi.command.team.removemember.RemoveCaseTeamGroup;
import org.cafienne.model.cmmn.actorapi.command.team.removemember.RemoveCaseTeamTenantRole;
import org.cafienne.model.cmmn.actorapi.command.team.removemember.RemoveCaseTeamUser;
import org.cafienne.model.cmmn.actorapi.command.team.setmember.SetCaseTeamGroup;
import org.cafienne.model.cmmn.actorapi.command.team.setmember.SetCaseTeamTenantRole;
import org.cafienne.model.cmmn.actorapi.command.team.setmember.SetCaseTeamUser;
import org.cafienne.model.cmmn.actorapi.event.CaseAppliedPlatformUpdate;
import org.cafienne.model.cmmn.actorapi.event.CaseDefinitionApplied;
import org.cafienne.model.cmmn.actorapi.event.CaseModified;
import org.cafienne.model.cmmn.actorapi.event.CaseOutputFilled;
import org.cafienne.model.cmmn.actorapi.event.file.*;
import org.cafienne.model.cmmn.actorapi.event.migration.*;
import org.cafienne.model.cmmn.actorapi.event.plan.PlanItemCreated;
import org.cafienne.model.cmmn.actorapi.event.plan.PlanItemTransitioned;
import org.cafienne.model.cmmn.actorapi.event.plan.RepetitionRuleEvaluated;
import org.cafienne.model.cmmn.actorapi.event.plan.RequiredRuleEvaluated;
import org.cafienne.model.cmmn.actorapi.event.plan.eventlistener.*;
import org.cafienne.model.cmmn.actorapi.event.plan.task.*;
import org.cafienne.model.cmmn.actorapi.event.plan.task.humantask.*;
import org.cafienne.model.cmmn.actorapi.event.plan.task.humantask.migration.HumanTaskDropped;
import org.cafienne.model.cmmn.actorapi.event.plan.task.humantask.migration.HumanTaskMigrated;
import org.cafienne.model.cmmn.actorapi.event.team.deprecated.member.CaseOwnerAdded;
import org.cafienne.model.cmmn.actorapi.event.team.deprecated.member.CaseOwnerRemoved;
import org.cafienne.model.cmmn.actorapi.event.team.deprecated.member.TeamRoleCleared;
import org.cafienne.model.cmmn.actorapi.event.team.deprecated.member.TeamRoleFilled;
import org.cafienne.model.cmmn.actorapi.event.team.deprecated.user.TeamMemberAdded;
import org.cafienne.model.cmmn.actorapi.event.team.deprecated.user.TeamMemberRemoved;
import org.cafienne.model.cmmn.actorapi.event.team.group.CaseTeamGroupAdded;
import org.cafienne.model.cmmn.actorapi.event.team.group.CaseTeamGroupChanged;
import org.cafienne.model.cmmn.actorapi.event.team.group.CaseTeamGroupRemoved;
import org.cafienne.model.cmmn.actorapi.event.team.tenantrole.CaseTeamTenantRoleAdded;
import org.cafienne.model.cmmn.actorapi.event.team.tenantrole.CaseTeamTenantRoleChanged;
import org.cafienne.model.cmmn.actorapi.event.team.tenantrole.CaseTeamTenantRoleRemoved;
import org.cafienne.model.cmmn.actorapi.event.team.user.CaseTeamUserAdded;
import org.cafienne.model.cmmn.actorapi.event.team.user.CaseTeamUserChanged;
import org.cafienne.model.cmmn.actorapi.event.team.user.CaseTeamUserRemoved;
import org.cafienne.model.cmmn.actorapi.response.*;
import org.cafienne.model.cmmn.actorapi.response.migration.MigrationStartedResponse;

public class CaseSerializers {
    public static void register() {
        registerCaseCommands();
        registerCaseEvents();
        registerCaseResponses();
    }

    private static void registerCaseCommands() {
        CafienneSerializer.addManifestWrapper(StartCase.class, StartCase::new);
        CafienneSerializer.addManifestWrapper(ReactivateCase.class, ReactivateCase::new);
        CafienneSerializer.addManifestWrapper(MigrateDefinition.class, MigrateDefinition::new);
        CafienneSerializer.addManifestWrapper(MigrateCaseDefinition.class, MigrateCaseDefinition::new);
        CafienneSerializer.addManifestWrapper(SwitchDebugMode.class, SwitchDebugMode::new);
        registerCasePlanCommands();
        registerCaseFileCommands();
        registerCaseTeamCommands();
        registerHumanTaskCommands();
    }

    private static void registerCasePlanCommands() {
        CafienneSerializer.addManifestWrapper(AddDiscretionaryItem.class, AddDiscretionaryItem::new);
        CafienneSerializer.addManifestWrapper(GetDiscretionaryItems.class, GetDiscretionaryItems::new);
        CafienneSerializer.addManifestWrapper(MakeCaseTransition.class, MakeCaseTransition::new);
        CafienneSerializer.addManifestWrapper(MakePlanItemTransition.class, MakePlanItemTransition::new);
        CafienneSerializer.addManifestWrapper(HandleTaskImplementationTransition.class, HandleTaskImplementationTransition::new);
        CafienneSerializer.addManifestWrapper(CompleteTask.class, CompleteTask::new);
        CafienneSerializer.addManifestWrapper(FailTask.class, FailTask::new);
        CafienneSerializer.addManifestWrapper(RaiseEvent.class, RaiseEvent::new);
    }

    private static void registerCaseFileCommands() {
        CafienneSerializer.addManifestWrapper(CreateCaseFileItem.class, CreateCaseFileItem::new);
        CafienneSerializer.addManifestWrapper(DeleteCaseFileItem.class, DeleteCaseFileItem::new);
        CafienneSerializer.addManifestWrapper(ReplaceCaseFileItem.class, ReplaceCaseFileItem::new);
        CafienneSerializer.addManifestWrapper(UpdateCaseFileItem.class, UpdateCaseFileItem::new);
    }

    private static void registerCaseTeamCommands() {
        CafienneSerializer.addManifestWrapper(DeprecatedUpsert.class, DeprecatedUpsert::new);
        CafienneSerializer.addManifestWrapper(SetCaseTeamUser.class, SetCaseTeamUser::new);
        CafienneSerializer.addManifestWrapper(SetCaseTeamTenantRole.class, SetCaseTeamTenantRole::new);
        CafienneSerializer.addManifestWrapper(SetCaseTeamGroup.class, SetCaseTeamGroup::new);
        CafienneSerializer.addManifestWrapper(RemoveCaseTeamUser.class, RemoveCaseTeamUser::new);
        CafienneSerializer.addManifestWrapper(RemoveCaseTeamGroup.class, RemoveCaseTeamGroup::new);
        CafienneSerializer.addManifestWrapper(RemoveCaseTeamTenantRole.class, RemoveCaseTeamTenantRole::new);
        CafienneSerializer.addManifestWrapper(SetCaseTeam.class, SetCaseTeam::new);
    }

    private static void registerHumanTaskCommands() {
        CafienneSerializer.addManifestWrapper(AssignTask.class, AssignTask::new);
        CafienneSerializer.addManifestWrapper(ClaimTask.class, ClaimTask::new);
        CafienneSerializer.addManifestWrapper(CompleteHumanTask.class, CompleteHumanTask::new);
        CafienneSerializer.addManifestWrapper(DelegateTask.class, DelegateTask::new);
        CafienneSerializer.addManifestWrapper(FillTaskDueDate.class, FillTaskDueDate::new);
        CafienneSerializer.addManifestWrapper(RevokeTask.class, RevokeTask::new);
        CafienneSerializer.addManifestWrapper(SaveTaskOutput.class, SaveTaskOutput::new);
        CafienneSerializer.addManifestWrapper(ValidateTaskOutput.class, ValidateTaskOutput::new);
    }
    private static void registerCaseEvents() {
        CafienneSerializer.addManifestWrapper(CaseDefinitionApplied.class, CaseDefinitionApplied::new);
        CafienneSerializer.addManifestWrapper(CaseModified.class, CaseModified::new);
        CafienneSerializer.addManifestWrapper(CaseAppliedPlatformUpdate.class, CaseAppliedPlatformUpdate::new);
        CafienneSerializer.addManifestWrapper(EngineVersionChanged.class, EngineVersionChanged::new);
        CafienneSerializer.addManifestWrapper(CaseDefinitionMigrated.class, CaseDefinitionMigrated::new);
        CafienneSerializer.addManifestWrapper(CaseOutputFilled.class, CaseOutputFilled::new);
        registerCaseTeamEvents();
        registerCasePlanEvents();
        registerCaseFileEvents();
    }

    private static void registerCaseTeamEvents() {
        registerCaseTeamMemberEvents();
        registerDeprecatedCaseTeamEvents();
    }

    private static void registerCaseTeamMemberEvents() {
        CafienneSerializer.addManifestWrapper(CaseTeamUserAdded.class, CaseTeamUserAdded::new);
        CafienneSerializer.addManifestWrapper(CaseTeamUserChanged.class, CaseTeamUserChanged::new);
        CafienneSerializer.addManifestWrapper(CaseTeamUserRemoved.class, CaseTeamUserRemoved::new);
        CafienneSerializer.addManifestWrapper(CaseTeamGroupAdded.class, CaseTeamGroupAdded::new);
        CafienneSerializer.addManifestWrapper(CaseTeamGroupChanged.class, CaseTeamGroupChanged::new);
        CafienneSerializer.addManifestWrapper(CaseTeamGroupRemoved.class, CaseTeamGroupRemoved::new);
        CafienneSerializer.addManifestWrapper(CaseTeamTenantRoleAdded.class, CaseTeamTenantRoleAdded::new);
        CafienneSerializer.addManifestWrapper(CaseTeamTenantRoleChanged.class, CaseTeamTenantRoleChanged::new);
        CafienneSerializer.addManifestWrapper(CaseTeamTenantRoleRemoved.class, CaseTeamTenantRoleRemoved::new);
    }

    private static void registerDeprecatedCaseTeamEvents() {
        // The newest old ones
        CafienneSerializer.addManifestWrapper(TeamRoleFilled.class, TeamRoleFilled::new);
        CafienneSerializer.addManifestWrapper(TeamRoleCleared.class, TeamRoleCleared::new);
        CafienneSerializer.addManifestWrapper(CaseOwnerAdded.class, CaseOwnerAdded::new);
        CafienneSerializer.addManifestWrapper(CaseOwnerRemoved.class, CaseOwnerRemoved::new);
        // Even older ones
        CafienneSerializer.addManifestWrapper(TeamMemberAdded.class, TeamMemberAdded::new);
        CafienneSerializer.addManifestWrapper(TeamMemberRemoved.class, TeamMemberRemoved::new);
    }

    private static void registerCasePlanEvents() {
        CafienneSerializer.addManifestWrapper(PlanItemCreated.class, PlanItemCreated::new);
        CafienneSerializer.addManifestWrapper(PlanItemTransitioned.class, PlanItemTransitioned::new);
        CafienneSerializer.addManifestWrapper(PlanItemMigrated.class, PlanItemMigrated::new);
        CafienneSerializer.addManifestWrapper(PlanItemDropped.class, PlanItemDropped::new);
        CafienneSerializer.addManifestWrapper(PlanItemMoved.class, PlanItemMoved::new);
        CafienneSerializer.addManifestWrapper(PlanItemMoving.class, PlanItemMoving::new);
        CafienneSerializer.addManifestWrapper(RepetitionRuleEvaluated.class, RepetitionRuleEvaluated::new);
        CafienneSerializer.addManifestWrapper(RequiredRuleEvaluated.class, RequiredRuleEvaluated::new);
        CafienneSerializer.addManifestWrapper(TaskInputFilled.class, TaskInputFilled::new);
        CafienneSerializer.addManifestWrapper(TaskOutputFilled.class, TaskOutputFilled::new);
        CafienneSerializer.addManifestWrapper(TaskImplementationStarted.class, TaskImplementationStarted::new);
        CafienneSerializer.addManifestWrapper(TaskCommandRejected.class, TaskCommandRejected::new);
        CafienneSerializer.addManifestWrapper(TaskImplementationNotStarted.class, TaskImplementationNotStarted::new);
        CafienneSerializer.addManifestWrapper(TaskImplementationReactivated.class, TaskImplementationReactivated::new);
        CafienneSerializer.addManifestWrapper(TimerSet.class, TimerSet::new);
        CafienneSerializer.addManifestWrapper(TimerCompleted.class, TimerCompleted::new);
        CafienneSerializer.addManifestWrapper(TimerTerminated.class, TimerTerminated::new);
        CafienneSerializer.addManifestWrapper(TimerSuspended.class, TimerSuspended::new);
        CafienneSerializer.addManifestWrapper(TimerResumed.class, TimerResumed::new);
        CafienneSerializer.addManifestWrapper(TimerDropped.class, TimerDropped::new);
        CafienneSerializer.addManifestWrapper(HumanTaskCreated.class, HumanTaskCreated::new);
        CafienneSerializer.addManifestWrapper(HumanTaskActivated.class, HumanTaskActivated::new);
        CafienneSerializer.addManifestWrapper(HumanTaskInputSaved.class, HumanTaskInputSaved::new);
        CafienneSerializer.addManifestWrapper(HumanTaskOutputSaved.class, HumanTaskOutputSaved::new);
        CafienneSerializer.addManifestWrapper(HumanTaskAssigned.class, HumanTaskAssigned::new);
        CafienneSerializer.addManifestWrapper(HumanTaskClaimed.class, HumanTaskClaimed::new);
        CafienneSerializer.addManifestWrapper(HumanTaskCompleted.class, HumanTaskCompleted::new);
        CafienneSerializer.addManifestWrapper(HumanTaskDelegated.class, HumanTaskDelegated::new);
        CafienneSerializer.addManifestWrapper(HumanTaskDueDateFilled.class, HumanTaskDueDateFilled::new);
        CafienneSerializer.addManifestWrapper(HumanTaskOwnerChanged.class, HumanTaskOwnerChanged::new);
        CafienneSerializer.addManifestWrapper(HumanTaskResumed.class, HumanTaskResumed::new);
        CafienneSerializer.addManifestWrapper(HumanTaskRevoked.class, HumanTaskRevoked::new);
        CafienneSerializer.addManifestWrapper(HumanTaskSuspended.class, HumanTaskSuspended::new);
        CafienneSerializer.addManifestWrapper(HumanTaskTerminated.class, HumanTaskTerminated::new);
        CafienneSerializer.addManifestWrapper(HumanTaskMigrated.class, HumanTaskMigrated::new);
        CafienneSerializer.addManifestWrapper(HumanTaskDropped.class, HumanTaskDropped::new);
    }

    private static void registerCaseFileEvents() {
        CafienneSerializer.addManifestWrapper(CaseFileItemCreated.class, CaseFileItemCreated::new);
        CafienneSerializer.addManifestWrapper(CaseFileItemUpdated.class, CaseFileItemUpdated::new);
        CafienneSerializer.addManifestWrapper(CaseFileItemReplaced.class, CaseFileItemReplaced::new);
        CafienneSerializer.addManifestWrapper(CaseFileItemDeleted.class, CaseFileItemDeleted::new);
        CafienneSerializer.addManifestWrapper(CaseFileItemChildRemoved.class, CaseFileItemChildRemoved::new);
        // Note: CaseFileItemTransitioned event cannot be deleted, since sub class events above were introduced only in 1.1.9
        CafienneSerializer.addManifestWrapper(CaseFileItemTransitioned.class, CaseFileItemTransitioned::new);
        CafienneSerializer.addManifestWrapper(BusinessIdentifierSet.class, BusinessIdentifierSet::new);
        CafienneSerializer.addManifestWrapper(BusinessIdentifierCleared.class, BusinessIdentifierCleared::new);
        CafienneSerializer.addManifestWrapper(CaseFileItemMigrated.class, CaseFileItemMigrated::new);
        CafienneSerializer.addManifestWrapper(CaseFileItemDropped.class, CaseFileItemDropped::new);
    }

    private static void registerCaseResponses() {
        CafienneSerializer.addManifestWrapper(AddDiscretionaryItemResponse.class, AddDiscretionaryItemResponse::new);
        CafienneSerializer.addManifestWrapper(GetDiscretionaryItemsResponse.class, GetDiscretionaryItemsResponse::new);
        CafienneSerializer.addManifestWrapper(CaseStartedResponse.class, CaseStartedResponse::new);
        CafienneSerializer.addManifestWrapper(MigrationStartedResponse.class, MigrationStartedResponse::new);
        CafienneSerializer.addManifestWrapper(CaseResponse.class, CaseResponse::new);
        CafienneSerializer.addManifestWrapper(CaseNotModifiedResponse.class, CaseNotModifiedResponse::new);
        CafienneSerializer.addManifestWrapper(HumanTaskResponse.class, HumanTaskResponse::new);
        CafienneSerializer.addManifestWrapper(HumanTaskValidationResponse.class, HumanTaskValidationResponse::new);
    }
}
