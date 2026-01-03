package org.cafienne.persistence.eventdb.schema.postgres

import org.cafienne.persistence.eventdb.schema.EventDBSchemaProvider
import org.cafienne.persistence.flyway.SchemaMigrator

object PostgresEventDBSchema extends EventDBSchemaProvider {
  override def scripts(tablePrefix: String): Seq[SchemaMigrator] = Seq(new V1_1_13__AddTimerService(tablePrefix: String), new V1_1_16__CreateAkkaSchema(tablePrefix: String), new V1_1_39__AddTimerTableMetadataColumn(tablePrefix: String))
}
