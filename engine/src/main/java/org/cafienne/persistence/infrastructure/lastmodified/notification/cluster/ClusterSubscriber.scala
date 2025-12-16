package org.cafienne.persistence.infrastructure.lastmodified.notification.cluster

import org.apache.pekko.actor.Actor
import org.apache.pekko.cluster.pubsub.DistributedPubSub
import org.apache.pekko.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import org.cafienne.actormodel.message.event.ActorModified
import org.cafienne.persistence.infrastructure.lastmodified.notification.LastModifiedSubscriber
import org.cafienne.persistence.querydb.schema.QueryDB

class ClusterSubscriber(val queryDB: QueryDB) extends Actor with LastModifiedSubscriber {
  private val mediator = DistributedPubSub(context.system).mediator
  mediator ! Subscribe(queryDB.topic, self)

  override def receive: Receive = {
    case am: ActorModified[_, _] => handleActorModified(am)
    case SubscribeAck(Subscribe(queryDB.topic, None, `self`)) => //println("Subscription is acknowledged")
    case other => //println("Received unclear other message " + other)
  }
}
