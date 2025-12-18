package org.cafienne.persistence.infrastructure.lastmodified.notification

import org.cafienne.actormodel.message.event.ActorModified

trait LastModifiedPublisher {
  def publish(event: ActorModified[_, _]): Unit
}
