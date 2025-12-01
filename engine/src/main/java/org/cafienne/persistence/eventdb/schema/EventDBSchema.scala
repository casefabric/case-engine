package org.cafienne.persistence.eventdb.schema

import org.cafienne.infrastructure.config.persistence.PersistenceConfig
import org.cafienne.persistence.flyway.{DBSchema, SchemaMigrator}
import org.flywaydb.core.api.resolver.ResolvedMigration
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.migration.api.flyway.VersionedMigration

class EventDBSchema(val config: PersistenceConfig, dbConfig: DatabaseConfig[JdbcProfile], scriptProvider: EventDBSchemaProvider) extends DBSchema {
  override val databaseDescription: String = "EventDB"
  override val flywaySchemaTableName: String = config.eventDB.schemaHistoryTable

  override def scripts(tablePrefix: String): Seq[ResolvedMigration] = scriptProvider.scripts(tablePrefix)

  override def migrationScripts(tablePrefix: String): Seq[VersionedMigration[_]] = scriptProvider.scripts(tablePrefix).asInstanceOf[Seq[VersionedMigration[_]]]
}

/**
 * Simple trait for providing DB specific scripts
 */
trait EventDBSchemaProvider {
  def scripts(tablePrefix: String): Seq[SchemaMigrator]
}
