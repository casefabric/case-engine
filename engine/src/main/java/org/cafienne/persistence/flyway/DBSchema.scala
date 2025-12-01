package org.cafienne.persistence.flyway

import org.cafienne.infrastructure.config.persistence.PersistenceConfig
import org.flywaydb.core.api.resolver.ResolvedMigration
import slick.migration.api.flyway.VersionedMigration

trait DBSchema {
  val config: PersistenceConfig

  val databaseDescription: String

  val flywaySchemaTableName: String

  val baseLineVersion: String = "0.0.0"

  def scripts(tablePrefix: String): Seq[ResolvedMigration]

  def migrationScripts(tablePrefix: String): Seq[VersionedMigration[_]]
}
