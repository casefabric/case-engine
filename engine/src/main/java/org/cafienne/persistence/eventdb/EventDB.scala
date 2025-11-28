package org.cafienne.persistence.eventdb

import com.typesafe.scalalogging.LazyLogging
import org.cafienne.infrastructure.config.persistence.PersistenceConfig
import org.cafienne.infrastructure.config.persistence.eventdb.Profile
import org.cafienne.persistence.eventdb.schema.EventDBSchema
import org.cafienne.persistence.eventdb.schema.h2.H2EventDBSchema
import org.cafienne.persistence.eventdb.schema.postgres.PostgresEventDBSchema
import org.cafienne.persistence.eventdb.schema.sqlserver.SQLServerEventDBSchema
import org.cafienne.persistence.flyway.{DB, DBSchema, FlywayRunner}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class EventDB(val config: PersistenceConfig, val dbConfig: DatabaseConfig[JdbcProfile]) extends DB with LazyLogging {
  override val databaseDescription: String = "EventDB"

  override def schema: DBSchema = {
    if (config.initializeDatabaseSchemas && config.eventDB.isJDBC) {
      val scriptProvider = config.eventDB.jdbcConfig.profile match {
        case Profile.Postgres => PostgresEventDBSchema
        case Profile.SQLServer => SQLServerEventDBSchema
        case Profile.H2 => H2EventDBSchema
        case Profile.Unsupported => throw new IllegalArgumentException("This type of profile is not supported")
      }

      new EventDBSchema(config, dbConfig, scriptProvider)
    } else {
      throw new Error("Event Database is not of type JDBC, and cannot produce a JDBC based Database Schema")
    }
  }

  if (config.initializeDatabaseSchemas && config.eventDB.isJDBC) {
    new FlywayRunner(this).initialize()
  }
}
