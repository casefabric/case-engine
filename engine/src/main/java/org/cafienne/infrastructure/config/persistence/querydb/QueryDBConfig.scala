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

package org.cafienne.infrastructure.config.persistence.querydb

import com.typesafe.config.Config
import org.apache.pekko.stream.RestartSettings
import org.cafienne.infrastructure.config.persistence.PersistenceConfig
import org.cafienne.infrastructure.config.util.{ChildConfigReader, MandatoryConfig}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile


class QueryDBConfig(val parent: PersistenceConfig) extends MandatoryConfig {
  def path = "query-db"

  /**
   * The cluster-key can be used to identify a query database within a clustered setup of the case engine
   *  In a scenario of 2 QueryDB instances and 10 cluster nodes, 4 nodes could be using the first query database, and the other 6 nodes the second database/
   *  By identifying the query database in use, each node knows how to subscribe with the proper database writer to receive the ActorModified updates.
   *  Default value is "query-db"
   */
  lazy val key: String = readString("cluster-key", "query-db")

  override lazy val config: Config = {
    if (parent.parent.config.hasPath(path)) {
      // For compatibility, also try to read from 'cafienne' config itself, if persistence is not available
      //  Note: this was the default up until version 1.1.33
      warn("""QueryDB configuration can be put inside the persistence configuration like:
             |cafienne {
             |  persistence {
             |    query-db = {
             |      ...
             |    }
             |  }
             |}""".stripMargin)
      parent.parent.config.getConfig(path)
    } else {
      parent.config.getConfig(path)
    }
  }

  lazy val jdbcConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("", config)

  lazy val schemaHistoryTable: String = readString("schema-history-table", "flyway_schema_history")

  override val msg = "Cafienne Query Database is not configured. Check local.conf for 'cafienne.query-db' settings"

  lazy val restartSettings: RestartSettings = new RestartConfig(this).settings

  lazy val h2WebServer: H2WebServerConfig = new H2WebServerConfig(this)

  lazy val readJournal: String = {
    val foundJournal = readString("read-journal")
    logger.warn(s"Obtaining read-journal settings from 'cafienne.querydb.read-journal' = $foundJournal is deprecated; please place these settings in 'cafienne.read-journal' instead")
    foundJournal
  }
}

class H2WebServerConfig(val parent: QueryDBConfig) extends ChildConfigReader {
  val path: String = "h2-webserver"

  private lazy val defaultPort = if (parent.readBoolean("debug", default = false)) 8082 else -1

  lazy val port: Integer = readInt("port", defaultPort)

  lazy val enabled: Boolean = port > 0
}


