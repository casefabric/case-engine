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

package org.cafienne.service.storage.actormodel.state

import org.cafienne.actormodel.ActorMetadata
import org.cafienne.persistence.querydb.schema.table.CaseTables
import org.cafienne.persistence.querydb.schema.table.userregistration.{ConsentGroupTables, TenantTables}

trait TenantState extends StorageActorState
  with TenantTables
  with CaseTables
  with ConsentGroupTables {

  override def findCascadingChildren(): Seq[ActorMetadata] = {
    printLogMessage("Running tenant query on cases and groups")

    val storedCases = readCases(metadata.actorId)
    val storedGroups = readGroups(metadata.actorId)

    val cases = storedCases.map(id => metadata.caseMember(id))
    val groups = storedGroups.map(id => metadata.groupMember(id))
    printLogMessage(s"Found ${cases.length} cases and ${groups.length} groups")
    cases ++ groups
  }

  override def clearQueryData(): Unit = deleteTenant(actorId)

  import dbConfig.profile.api._

  private def deleteTenant(tenant: String): Unit = {
    addStatement(TableQuery[UserRoleTable].filter(_.tenant === tenant).delete)
    addStatement(TableQuery[TenantTable].filter(_.name === tenant).delete)
    commit()
  }

  private def readCases(tenant: String): Seq[String] = {
    val query = TableQuery[CaseInstanceTable].filter(_.tenant === tenant).filter(_.parentCaseId === "").map(_.id).distinct
    runSync(query.result)
  }

  private def readGroups(tenant: String): Seq[String] = {
    val query = TableQuery[ConsentGroupTable].filter(_.tenant === tenant).map(_.id).distinct
    runSync(query.result)
  }
}
