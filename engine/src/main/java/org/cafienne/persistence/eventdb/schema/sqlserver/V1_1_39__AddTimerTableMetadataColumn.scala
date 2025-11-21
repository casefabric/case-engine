package org.cafienne.persistence.eventdb.schema.sqlserver

import org.cafienne.persistence.eventdb.schema.ClassicEventDBSchemaScript

class V1_1_39__AddTimerTableMetadataColumn(tablePrefix: String) extends ClassicEventDBSchemaScript {
  val version = "1.1.39"

  val description = "Add metadata column to Timer table"

  override def sql: String = s"""ALTER TABLE [${tablePrefix}timer] ADD [metadata] varchar(MAX) DEFAULT '';"""
}
