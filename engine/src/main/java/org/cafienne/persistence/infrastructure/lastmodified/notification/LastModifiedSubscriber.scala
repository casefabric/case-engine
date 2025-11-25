package org.cafienne.persistence.infrastructure.lastmodified.notification

import org.cafienne.actormodel.message.event.ActorModified
import org.cafienne.model.cmmn.actorapi.event.CaseModified
import org.cafienne.persistence.querydb.schema.QueryDB
import org.cafienne.usermanagement.consentgroup.actorapi.event.ConsentGroupModified
import org.cafienne.usermanagement.tenant.actorapi.event.TenantModified

trait LastModifiedSubscriber {
  val queryDB: QueryDB

  def handleActorModified(event: ActorModified[_, _]): Unit = event match {
    case cm: CaseModified => queryDB.caseLastModifiedRegistration.handle(cm)
    case tm: TenantModified => queryDB.tenantLastModifiedRegistration.handle(tm)
    case gm: ConsentGroupModified => queryDB.groupLastModifiedRegistration.handle(gm)
    case other => println("Received last modified of unknown type " + other.getClass.getName)
  }
}
