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

package org.cafienne.persistence.querydb.materializer.slick

import com.typesafe.scalalogging.LazyLogging
import org.cafienne.persistence.flyway.FlywayRunner
import org.cafienne.persistence.querydb.materializer.cases.CaseEventSink
import org.cafienne.persistence.querydb.materializer.consentgroup.ConsentGroupEventSink
import org.cafienne.persistence.querydb.materializer.tenant.TenantEventSink
import org.cafienne.persistence.querydb.schema.QueryDB
import org.cafienne.system.CaseSystem

class QueryDBWriter(val queryDB: QueryDB) extends LazyLogging {
  def startEventSinks(caseSystem: CaseSystem): Unit = {
    new CaseEventSink(queryDB.publisher, caseSystem, queryDB).start()
    new TenantEventSink(queryDB.publisher, caseSystem, queryDB).start()
    new ConsentGroupEventSink(queryDB.publisher, caseSystem, queryDB).start()
  }

  def initializeDatabaseSchema(): Unit = {
    new FlywayRunner(queryDB).initialize()
  }

  if (queryDB.config.initializeDatabaseSchemas) {
    initializeDatabaseSchema()
  }
}
