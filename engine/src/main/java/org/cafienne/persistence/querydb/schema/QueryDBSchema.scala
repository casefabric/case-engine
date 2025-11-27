package org.cafienne.persistence.querydb.schema

import com.typesafe.scalalogging.LazyLogging
import org.cafienne.infrastructure.config.persistence.PersistenceConfig
import org.cafienne.persistence.infrastructure.jdbc.schema.{CustomMigrationInfo, QueryDBSchemaVersion, SlickMigrationExtensions}
import org.cafienne.persistence.querydb.schema.versions._
import org.flywaydb.core.api.output.MigrateResult
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.migration.api.Migration
import slick.migration.api.flyway.{MigrationInfo, SlickFlyway}

class QueryDBSchema(val config: PersistenceConfig, val dbConfig: DatabaseConfig[JdbcProfile]) extends SlickMigrationExtensions with LazyLogging {

  override val tablePrefix: String = config.tablePrefix

  def initializeDatabaseSchema(): MigrateResult = {
    useSchema(
      new QueryDB_1_0_0(dbConfig, tablePrefix),
      new QueryDB_1_1_5(dbConfig, tablePrefix),
      new QueryDB_1_1_6(dbConfig, tablePrefix),
      new QueryDB_1_1_10(dbConfig, tablePrefix),
      new QueryDB_1_1_11(dbConfig, tablePrefix),
      new QueryDB_1_1_16(dbConfig, tablePrefix),
      new QueryDB_1_1_18(dbConfig, tablePrefix),
      new QueryDB_1_1_22(dbConfig, tablePrefix),
      new QueryDB_1_1_36(dbConfig, tablePrefix),
    )
  }

  implicit val infoProvider: MigrationInfo.Provider[Migration] = CustomMigrationInfo.provider

  private def useSchema(schemas: QueryDBSchemaVersion*): MigrateResult = {
    try {
      val flywayConfiguration = SlickFlyway(db)(schemas.flatMap(schema => schema.getScript))
        .baselineOnMigrate(true)
        .baselineDescription("CaseFabric QueryDB")
        .baselineVersion("0.0.0")
        .table(config.queryDB.schemaHistoryTable)

      // Create a connection and run migration
      val flyway = flywayConfiguration.load()
      flyway.migrate()
    } catch {
      case e: Exception =>
        logger.error("Encountered a fatal database schema error", e)
        throw e
    }
  }
}
