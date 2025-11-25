package org.cafienne.persistence.infrastructure.lastmodified.notification.singleton

import org.cafienne.actormodel.message.event.ActorModified
import org.cafienne.persistence.infrastructure.lastmodified.notification.{LastModifiedPublisher, LastModifiedSubscriber}
import org.cafienne.persistence.querydb.schema.QueryDB

class InMemoryPublisher(val queryDB: QueryDB) extends LastModifiedPublisher with LastModifiedSubscriber {

  override def publish(event: ActorModified[_, _]): Unit = {
    handleActorModified(event)
  }
}
