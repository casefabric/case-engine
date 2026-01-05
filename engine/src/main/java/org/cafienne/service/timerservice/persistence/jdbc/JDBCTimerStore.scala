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

package org.cafienne.service.timerservice.persistence.jdbc

import org.apache.pekko.Done
import org.apache.pekko.persistence.query.Offset
import org.cafienne.infrastructure.cqrs.offset.OffsetRecord
import org.cafienne.persistence.infrastructure.jdbc.cqrs.JDBCOffsetStorage
import org.cafienne.service.timerservice.Timer
import org.cafienne.service.timerservice.persistence.TimerStore
import org.cafienne.system.CaseSystem
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import java.time.Instant
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class JDBCTimerStore(caseSystem: CaseSystem, val dbConfig: DatabaseConfig[JdbcProfile], val tablePrefix: String)
  extends TimerStore
    with JDBCOffsetStorage
    with TimerServiceTables {

  import dbConfig.profile.api._

  override implicit val ec: ExecutionContext = db.ioExecutionContext

  private def updateTimerRecords(records: Seq[TimerServiceRecord]): Unit = {
    //      println(s"Starting update for ${records.size} timers that lack ActorMetadata information")
    // Migrate timers without metadata by reading and constructing the metadata from the QueryDB
    val migrator = new MigratorAddingActorMetadataToTimers(caseSystem.queryDB)
    val enhancedTimerRecords = migrator.enrichWithActorMetadata(records)
    //      println(s"Enriched ${enhancedTimerRecords.size} timer records with ActorMetadata, starting update transaction")
    // Now create update statements for the timers
    val updates = enhancedTimerRecords.map(r => TableQuery[TimerServiceTable].insertOrUpdate(r))
    //      println(s"Starting update transaction")
    // And run the transaction
    Await.result(db.run(DBIO.sequence(updates).transactionally), 60.seconds)
    //      println(s"Completed migration of ${enhancedTimerRecords.size} timer records")
  }

  private def migrateTimersWithEmptyMetadata(): Unit = {
    val query = TableQuery[TimerServiceTable].filter(_.metadata === "")
//    println("Running q \n\n" + query.result.statements +"\n\n")
    val records = Await.result(db.run(query.result), 60.seconds)
    if (records.nonEmpty) {
      val batchSize = caseSystem.config.engine.timerService.migrationBatchSize
      // If there are more than 1000 records, we split the update into batches of 1000 each (because of the "IN" clause in the query that is run on the QueryDB)
      if (records.size > batchSize) {
        val batches = records.grouped(batchSize)
        batches.foreach(updateTimerRecords)
      } else {
        updateTimerRecords(records)
      }
    }
  }

  migrateTimersWithEmptyMetadata()

  override def getTimers(window: Instant): Future[Seq[Timer]] = {
    val query = TableQuery[TimerServiceTable].filter(_.moment <= window)
    db.run(query.distinct.result).map(records => records.map(record => Timer(record.caseInstanceId, record.metadata, record.timerId, record.moment, record.user)))
  }

  override def storeTimer(job: Timer, offset: Option[Offset]): Future[Done] = {
    logger.debug("Storing JDBC timer " + job.timerId + " for timestamp " + job.moment)
    val record = TimerServiceRecord(timerId = job.timerId, caseInstanceId = job.caseInstanceId, metadata = job.metadata.path, moment = job.moment, tenant = "", user = job.userId)
    commit(offset, TableQuery[TimerServiceTable].insertOrUpdate(record))
  }

  override def removeTimer(timerId: String, offset: Option[Offset]): Future[Done] = {
    logger.debug("Removing timer " + timerId)
    commit(offset, TableQuery[TimerServiceTable].filter(_.timerId === timerId).delete)
  }

  override def removeCaseTimers(caseInstanceId: String): Future[Done] = {
    logger.debug("Removing timers in case " + caseInstanceId)
    commit(None, TableQuery[TimerServiceTable].filter(_.caseInstanceId === caseInstanceId).delete)
  }

  private def commit(offset: Option[Offset], action: dbConfig.profile.api.DBIO[Int]): Future[Done] = {
    val offsetUpdate = offset.map(offset => TableQuery[OffsetStoreTable].insertOrUpdate(OffsetRecord(storageName, offset)))
    val updates = offsetUpdate.fold(Seq(action))(o => Seq(action, o))
    db.run(DBIO.sequence(updates).transactionally).map(_ => Done)
  }

  override def importTimers(list: Seq[Timer]): Unit = {
    val tx = list
      .map(job => TimerServiceRecord(timerId = job.timerId, caseInstanceId = job.caseInstanceId, metadata = job.metadata.path, moment = job.moment, tenant = "", user = job.userId))
      .map(record => TableQuery[TimerServiceTable].insertOrUpdate(record))
    Await.result(db.run(DBIO.sequence(tx).transactionally), 30.seconds)
  }
}
