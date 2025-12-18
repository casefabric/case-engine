package org.cafienne.service.timerservice.persistence.jdbc

import org.cafienne.persistence.querydb.query.cmmn.implementations.BaseQueryImpl
import org.cafienne.persistence.querydb.schema.QueryDB

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class MigratorAddingActorMetadataToTimers(queryDB: QueryDB) extends BaseQueryImpl(queryDB) {
  import dbConfig.profile.api._

  def enrichWithActorMetadata(timers: Seq[TimerServiceRecord]): Seq[TimerServiceRecord] = {
    val caseIdentifiers = timers.map(_.caseInstanceId)
    val query = for {
      root <- TableQuery[CaseIdentifierView].filter(_.id inSet caseIdentifiers).map(_.rootCaseId)
      ids <- TableQuery[CaseIdentifierView].filter(_.rootCaseId === root)
    } yield ids

    val records = Await.result(db.run(query.result), Duration.Inf)
    timers.map(timer => {
      val metadata = createCaseMetadata(timer.caseInstanceId, records)
      timer.copy(metadata = metadata.map(_.path).orNull)
    })
  }
}
