package org.cafienne.persistence.flyway

import com.typesafe.scalalogging.LazyLogging
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.migration.api.flyway.SlickFlyway

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class FlywayRunner(val database: DB) extends LazyLogging {
  private val dbConfig: DatabaseConfig[JdbcProfile] = database.dbConfig
  private val targetDescription: String = database.databaseDescription
  private val schema: DBSchema = database.schema
  private val tablePrefix: String = database.tablePrefix

  def initialize(): Unit = {
    val db = dbConfig.db
    val flywayConfiguration = SlickFlyway(db)(schema.migrationScripts(tablePrefix))
      .baselineOnMigrate(true)
      .baselineDescription(s"CaseFabric ${database.databaseDescription}")
      .baselineVersion(schema.baseLineVersion)
      .table(schema.flywaySchemaTableName)
//      .outOfOrder(true) // This one was in use for EventDB, but we think it is not necessary

    try {
      // Create a connection and run migration
      val flyway = flywayConfiguration.load()
      flyway.migrate()
    } catch {
      case e: Throwable =>
        import dbConfig.profile.api._
        logger.error(s"\n\nEncountered a fatal error in the $targetDescription database schema:\n\t${e.getLocalizedMessage.replace("\n", "\n\t")}\n")

        def fetchSchemaRecords(): Future[Unit] = {
          val my = sql"""select version, description, checksum from flyway_schema_history""".as[(String, String, String)]
          db.run(my).map(result => {
            val errorMessage =
              s"\n========== Expected Database Schema:\n ${schema.scripts(tablePrefix).map(s => s"[version = ${s.getVersion} | checksum = ${s.getChecksum} | description = ${s.getDescription}]").mkString("\n ")}\n" +
                s"\n========== Actual Database Schema History:\n ${result.map(s => s"[version = ${s._1} | checksum = ${s._3} | description = ${s._2}]").mkString("\n ")}\n"
            logger.error(errorMessage)
          })
        }

        Await.result(fetchSchemaRecords(), 5.seconds)
        throw e
    }
  }
}
