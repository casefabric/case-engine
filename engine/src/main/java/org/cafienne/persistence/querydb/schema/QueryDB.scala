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

package org.cafienne.persistence.querydb.schema

import com.typesafe.scalalogging.LazyLogging
import org.cafienne.infrastructure.config.persistence.PersistenceConfig
import org.cafienne.persistence.flyway.DB
import org.cafienne.persistence.querydb.materializer.cases.CaseEventSink
import org.cafienne.persistence.querydb.materializer.consentgroup.ConsentGroupEventSink
import org.cafienne.persistence.querydb.materializer.slick.QueryDBWriter
import org.cafienne.persistence.querydb.materializer.tenant.TenantEventSink
import org.cafienne.system.CaseSystem
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class QueryDB(val config: PersistenceConfig, val dbConfig: DatabaseConfig[JdbcProfile]) extends DB with LazyLogging {
  override val databaseDescription: String = "QueryDB"
  override val schema: QueryDBSchema = new QueryDBSchema(config, dbConfig)
  val writer = new QueryDBWriter(this)

  def startEventSinks(caseSystem: CaseSystem): Unit = {
    new CaseEventSink(caseSystem, writer).start()
    new TenantEventSink(caseSystem, writer).start()
    new ConsentGroupEventSink(caseSystem, writer).start()

    // When running with H2, you can start a debug web server on port 8082.
    checkH2InDebugMode()
  }

  private def checkH2InDebugMode(): Unit = {
    import org.h2.tools.Server

    if (config.queryDB.debug) {
      val port = "8082"
      logger.warn("Starting H2 Web Client on port " + port)
      Server.createWebServer("-web", "-webAllowOthers", "-webPort", port).start()
    }
  }
}
