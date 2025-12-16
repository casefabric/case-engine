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

package org.cafienne.system

import com.typesafe.scalalogging.LazyLogging
import org.apache.pekko.actor._
import org.cafienne.actormodel.identity.{CaseSystemIdentityRegistration, IdentityRegistration}
import org.cafienne.infrastructure.EngineVersion
import org.cafienne.infrastructure.config.CaseSystemConfig
import org.cafienne.infrastructure.config.util.SystemConfig
import org.cafienne.persistence.eventdb.EventDB
import org.cafienne.persistence.querydb.schema.QueryDB
import org.cafienne.system.bootstrap.BootstrapPlatformConfiguration

import scala.concurrent.ExecutionContextExecutor

/**
  *
  * A CaseSystem can be started either in Clustered mode, or as a Local system.
  * In the first case, it relies on actor clustering and sharding to manage the case instances
  * and forward messages to the proper case instance.
  * In the local scenario, the case system is run in-memory, and messages are forwarded by
  * a simple in-memory router.
  */
class CaseSystem(val systemConfig: SystemConfig, val system: ActorSystem, val queryDB: QueryDB) extends LazyLogging {
  lazy val config: CaseSystemConfig = systemConfig.cafienne

  // Start a schema checker for JDBC based event databases
  if (config.persistence.eventDB.isJDBC) {
    new EventDB(systemConfig.cafienne.persistence, systemConfig.cafienne.persistence.eventDB.databaseConfig)
  }

  lazy val hasClusteredConfiguration: Boolean = config.isClustered

  /**
   * Returns the BuildInfo as a string (containing JSON)
   */
  lazy val version = new EngineVersion

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  /**
    * Retrieve a router for case messages. This will forward the messages to the correct case instance
    */
  val engine: CaseEngineGateway = CaseEngineGateway.createGateway(this)

  val service: CaseServiceGateway = new CaseServiceGateway(this)

  lazy val identityRegistration: IdentityRegistration = new CaseSystemIdentityRegistration(this)

  // First, start platform bootstrap configuration
  BootstrapPlatformConfiguration.run(this)
}

object CaseSystem {
  val NAME: String = "Case-Engine-Actor-System"
  def DEFAULT: CaseSystem = apply(SystemConfig.DEFAULT)

  def apply(systemConfig: SystemConfig): CaseSystem = {
    val queryDB = new QueryDB(systemConfig.cafienne.persistence, systemConfig.cafienne.persistence.queryDB.jdbcConfig)
    new CaseSystem(systemConfig, ActorSystem(NAME, systemConfig.config), queryDB)
  }

  def apply(actorSystem: ActorSystem): CaseSystem = {
    val systemConfig = new SystemConfig(actorSystem.settings.config)
    val queryDB = new QueryDB(systemConfig.cafienne.persistence, systemConfig.cafienne.persistence.queryDB.jdbcConfig)
    new CaseSystem(systemConfig, actorSystem, queryDB)
  }
}
