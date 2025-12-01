package org.cafienne.persistence.querydb.schema

import com.typesafe.scalalogging.LazyLogging
import org.cafienne.infrastructure.config.persistence.PersistenceConfig
import org.cafienne.persistence.flyway.DBSchema
import org.cafienne.persistence.infrastructure.jdbc.schema.{CustomMigrationInfo, QueryDBSchemaVersion, SlickMigrationExtensions}
import org.cafienne.persistence.querydb.schema.versions._
import org.flywaydb.core.api.resolver.ResolvedMigration
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.migration.api.Migration
import slick.migration.api.flyway.{MigrationInfo, VersionedMigration}

class QueryDBSchema(val config: PersistenceConfig, val dbConfig: DatabaseConfig[JdbcProfile]) extends DBSchema with SlickMigrationExtensions with LazyLogging {

  override val databaseDescription: String = "QueryDB"

  override val tablePrefix: String = config.tablePrefix

  private val schemas: Seq[QueryDBSchemaVersion] = Seq(
    new QueryDB_1_0_0(dbConfig, tablePrefix),
    new QueryDB_1_1_5(dbConfig, tablePrefix),
    new QueryDB_1_1_6(dbConfig, tablePrefix),
    new QueryDB_1_1_10(dbConfig, tablePrefix),
    new QueryDB_1_1_11(dbConfig, tablePrefix),
    new QueryDB_1_1_16(dbConfig, tablePrefix),
    new QueryDB_1_1_18(dbConfig, tablePrefix),
    new QueryDB_1_1_22(dbConfig, tablePrefix),
    new QueryDB_1_1_36(dbConfig, tablePrefix)
  )

  override val flywaySchemaTableName: String = config.queryDB.schemaHistoryTable

  implicit val infoProvider: MigrationInfo.Provider[Migration] = CustomMigrationInfo.provider

  override def scripts(tablePrefix: String): Seq[ResolvedMigration] = schemas.flatMap(schema => schema.getScript)

  override def migrationScripts(tablePrefix: String): Seq[VersionedMigration[_]] = schemas.flatMap(schema => schema.getScript)
}
