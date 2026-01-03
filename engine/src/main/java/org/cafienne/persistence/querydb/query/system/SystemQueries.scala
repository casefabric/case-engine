package org.cafienne.persistence.querydb.query.system

import org.cafienne.actormodel.{ActorMetadata, ActorType}
import org.cafienne.actormodel.identity.UserIdentity
import org.cafienne.persistence.querydb.query.cmmn.implementations.BaseQueryImpl
import org.cafienne.persistence.querydb.query.exception.ActorSearchFailure
import org.cafienne.persistence.querydb.schema.QueryDB
import org.cafienne.persistence.querydb.schema.table.CaseTables
import org.cafienne.persistence.querydb.schema.table.userregistration.TenantTables

import scala.concurrent.Future

trait SystemQueries {
  def findActor(user: UserIdentity, actorId: String): Future[ActorMetadata]
}

class SystemQueriesImpl(queryDB: QueryDB) extends BaseQueryImpl(queryDB)
  with SystemQueries
  with CaseTables
  with TenantTables {

  import dbConfig.profile.api._

  override def findActor(user: UserIdentity, actorId: String): Future[ActorMetadata] = {
    val result = for {
      caseMetadata <- getCaseMetadata(actorId)
      processMetadata <- getProcessMetadata(actorId)
      tenantMetadata <- getTenantMetadata(actorId)
      groupMetadata <- getConsentGroupMetadata(actorId)
    } yield (caseMetadata, processMetadata, tenantMetadata, groupMetadata)

    result.map(r => {
      //      println("Results of metadata queries: " + r)
      if (r._1.nonEmpty) r._1.get
      else if (r._2.nonEmpty) r._2.get
      else if (r._3.nonEmpty) r._3.get
      else if (r._4.nonEmpty) r._4.get
      else throw ActorSearchFailure(actorId)
    })
  }

  private def getCaseMetadata(caseInstanceId: String) = {
    val query = queryCaseChain(caseInstanceId)
    db.run(query.result).map(records => createCaseMetadata(caseInstanceId, records))
  }

  private def getProcessMetadata(processId: String) = {
    val query = for {
      caseId <- TableQuery[PlanItemTable].filter(_.id === processId).map(_.caseInstanceId)
      caseChain <- queryCaseChain(caseId)
    } yield (caseId, caseChain)

    db.run(query.result).map(records => {
      if (records.isEmpty) {
        None
      } else {
        val caseId = records.map(_._1).head
        val chainRecords = records.map(_._2)
        createCaseMetadata(caseId, chainRecords).fold(noMetadata)(parent => Some(ActorMetadata(ActorType.Process, processId, parent)))
      }
    })
  }

  private def getTenantMetadata(tenant: String) = {
    db.run(TableQuery[TenantTable].filter(_.name === tenant).map(_.name).result).map(_.headOption).map(_.fold(noMetadata)(tenant => Some(ActorMetadata(ActorType.Tenant, tenant, null))))
  }

  private def getConsentGroupMetadata(groupId: String) = {
    db.run(TableQuery[ConsentGroupTable].filter(_.id === groupId).map(_.id).result).map(_.headOption).map(_.fold(noMetadata)(group => Some(ActorMetadata(ActorType.Group, group, null))))
  }
}
