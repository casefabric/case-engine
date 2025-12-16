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
import org.apache.pekko.actor.{Actor, Props}
import org.apache.pekko.persistence.query.Offset
import org.cafienne.infrastructure.config.persistence.PersistenceConfig
import org.cafienne.persistence.flyway.{DB, FlywayRunner}
import org.cafienne.persistence.infrastructure.jdbc.cqrs.JDBCOffsetStorage
import org.cafienne.persistence.infrastructure.lastmodified.LastModifiedRegistration
import org.cafienne.persistence.infrastructure.lastmodified.header.Headers
import org.cafienne.persistence.infrastructure.lastmodified.notification.LastModifiedPublisher
import org.cafienne.persistence.infrastructure.lastmodified.notification.cluster.{ClusterPublisher, ClusterSubscriber}
import org.cafienne.persistence.infrastructure.lastmodified.notification.singleton.InMemoryPublisher
import org.cafienne.persistence.querydb.materializer.QueryDBStorage
import org.cafienne.persistence.querydb.materializer.cases.CaseStorageTransaction
import org.cafienne.persistence.querydb.materializer.consentgroup.ConsentGroupStorageTransaction
import org.cafienne.persistence.querydb.materializer.slick.{QueryDBEventSinkManager, SlickCaseTransaction, SlickConsentGroupTransaction, SlickTenantTransaction}
import org.cafienne.persistence.querydb.materializer.tenant.TenantStorageTransaction
import org.cafienne.system.CaseSystem
import org.cafienne.util.Guid
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class QueryDB(val config: PersistenceConfig, val dbConfig: DatabaseConfig[JdbcProfile]) extends DB with QueryDBStorage with LazyLogging {
  override val databaseDescription: String = "QueryDB"
  override val schema: QueryDBSchema = new QueryDBSchema(config, dbConfig)
  val caseLastModifiedRegistration = new LastModifiedRegistration(Headers.CASE_LAST_MODIFIED)
  val tenantLastModifiedRegistration = new LastModifiedRegistration(Headers.TENANT_LAST_MODIFIED)
  val groupLastModifiedRegistration = new LastModifiedRegistration(Headers.CONSENT_GROUP_LAST_MODIFIED)
  lazy val topic: String = s"last-modified-registration-of-${config.queryDB.key}"

  // First check if we need to check and set the database schema
  if (config.initializeDatabaseSchemas) {
    new FlywayRunner(this).initialize()
  }

  def open(caseSystem: CaseSystem): Unit = {
    if (caseSystem.hasClusteredConfiguration) {
      caseSystem.service.createSingletonReference(classOf[ClusteredSingletonEventReader], "QueryDBEventSinkManager_for_" + config.queryDB.key)
      // Also subscribe with the publisher
      caseSystem.system.actorOf(Props(classOf[ClusterSubscriber], this), new Guid().toString)
    } else {
        new QueryDBEventSinkManager(this).startEventSinks(new InMemoryPublisher(this), caseSystem)
    }
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

class ClusteredSingletonEventReader(val caseSystem: CaseSystem) extends Actor with LazyLogging {
  private val queryDB = caseSystem.queryDB
  val publisher: LastModifiedPublisher = new ClusterPublisher(queryDB, caseSystem)
  new QueryDBEventSinkManager(caseSystem.queryDB).startEventSinks(publisher, caseSystem)

  override def receive: Receive = {
    case message => logger.debug("Receiving message in Singleton, but no clue where it is coming from: " + message.getClass.getName +" from " + sender())
  }
}