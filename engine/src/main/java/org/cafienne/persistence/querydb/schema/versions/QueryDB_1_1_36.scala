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

package org.cafienne.persistence.querydb.schema.versions

import org.cafienne.persistence.infrastructure.jdbc.schema.QueryDBSchemaVersion
import org.cafienne.persistence.querydb.schema.table.userregistration.ConsentGroupTables
import org.cafienne.persistence.querydb.schema.table.{CaseTables, TaskTables}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.migration.api.{SqlMigration, TableMigration}

class QueryDB_1_1_36(val dbConfig: DatabaseConfig[JdbcProfile], val tablePrefix: String)
  extends QueryDBSchemaVersion
    with ConsentGroupTables
    with TaskTables
    with CaseTables {

  val version = "1.1.36"
  val migrations = addIndexToConsentGroupMemberTableOnGroup.&(addCaseNameToTaskTable).&(fillTaskTable)

  import dbConfig.profile.api._

  private def addIndexToConsentGroupMemberTableOnGroup = TableMigration(TableQuery[ConsentGroupMemberTable]).addIndexes(_.indexGroup)

  private def addCaseNameToTaskTable = TableMigration(TableQuery[TaskTable]).addColumns(_.caseName)

  private def fillTaskTable = {
    val taskTable = TableMigration(TableQuery[TaskTable]).tableInfo.tableName // This will add a configured table-prefix to the table name (instead of hardcoding "task")
    val caseTable = TableMigration(TableQuery[CaseInstanceTable]).tableInfo.tableName
    SqlMigration(s"""update "$taskTable" set "case_name" = (select "case_name" from "$caseTable" where "$caseTable"."id" = "$taskTable"."case_instance_id")""")
  }
}
