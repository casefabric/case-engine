package org.cafienne.persistence.flyway

import org.cafienne.infrastructure.config.persistence.PersistenceConfig
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait DB {
  val config: PersistenceConfig
  val dbConfig: DatabaseConfig[JdbcProfile]
  val databaseDescription: String
  val tablePrefix: String = config.tablePrefix
  def schema: DBSchema
}
