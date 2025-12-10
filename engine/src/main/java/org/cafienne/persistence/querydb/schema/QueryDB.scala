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
import org.apache.pekko.persistence.query.Offset
import org.cafienne.infrastructure.config.persistence.PersistenceConfig
import org.cafienne.persistence.flyway.DB
import org.cafienne.persistence.infrastructure.jdbc.cqrs.JDBCOffsetStorage
import org.cafienne.persistence.infrastructure.lastmodified.LastModifiedRegistration
import org.cafienne.persistence.infrastructure.lastmodified.header.Headers
import org.cafienne.persistence.infrastructure.lastmodified.notification.LastModifiedPublisher
import org.cafienne.persistence.infrastructure.lastmodified.notification.singleton.InMemoryPublisher
import org.cafienne.persistence.querydb.materializer.QueryDBStorage
import org.cafienne.persistence.querydb.materializer.cases.CaseStorageTransaction
import org.cafienne.persistence.querydb.materializer.consentgroup.ConsentGroupStorageTransaction
import org.cafienne.persistence.querydb.materializer.slick.{QueryDBWriter, SlickCaseTransaction, SlickConsentGroupTransaction, SlickTenantTransaction}
import org.cafienne.persistence.querydb.materializer.tenant.TenantStorageTransaction
import org.cafienne.system.CaseSystem
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class QueryDB(val config: PersistenceConfig, val dbConfig: DatabaseConfig[JdbcProfile]) extends DB with QueryDBStorage with LazyLogging {
  override val databaseDescription: String = "QueryDB"
  override val schema: QueryDBSchema = new QueryDBSchema(config, dbConfig)
  val publisher: LastModifiedPublisher = new InMemoryPublisher(this)
  val writer = new QueryDBWriter(this)
  val caseLastModifiedRegistration = new LastModifiedRegistration(Headers.CASE_LAST_MODIFIED)
  val tenantLastModifiedRegistration = new LastModifiedRegistration(Headers.TENANT_LAST_MODIFIED)
  val groupLastModifiedRegistration = new LastModifiedRegistration(Headers.CONSENT_GROUP_LAST_MODIFIED)

  def open(caseSystem: CaseSystem): Unit = {
    writer.startEventSinks(caseSystem)
  }

  override def createCaseTransaction(): CaseStorageTransaction = new SlickCaseTransaction(this)

  override def createConsentGroupTransaction(): ConsentGroupStorageTransaction = new SlickConsentGroupTransaction(this)

  override def createTenantTransaction(): TenantStorageTransaction = new SlickTenantTransaction(this)

  override def getOffset(offsetName: String): Future[Offset] = new JDBCOffsetStorage {
    override val tablePrefix: String = schema.tablePrefix
    override val storageName: String = offsetName
    override lazy val dbConfig = schema.dbConfig
    override implicit val ec: ExecutionContext = db.ioExecutionContext
  }.getOffset

  // When running with H2, you can start a debug web server on port 8082.
  if (config.queryDB.h2WebServer.enabled) {
    import org.h2.tools.Server
    val port = String.valueOf(config.queryDB.h2WebServer.port)
    logger.warn("Starting H2 Web Client on port " + port)
    Server.createWebServer("-web", "-webAllowOthers", "-webPort", port).start()
  }
}
