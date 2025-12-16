package org.cafienne.persistence.infrastructure.lastmodified.notification.cluster

import org.apache.pekko.actor.ActorRef
import org.apache.pekko.cluster.pubsub.DistributedPubSub
import org.apache.pekko.cluster.pubsub.DistributedPubSubMediator.Publish
import org.cafienne.actormodel.message.event.ActorModified
import org.cafienne.persistence.infrastructure.lastmodified.notification.LastModifiedPublisher
import org.cafienne.persistence.querydb.schema.QueryDB
import org.cafienne.system.CaseSystem

class ClusterPublisher(val queryDB: QueryDB, caseSystem: CaseSystem) extends LastModifiedPublisher {
  val mediator: ActorRef = DistributedPubSub(caseSystem.system).mediator

  override def publish(event: ActorModified[_, _]): Unit = {
//    println(s"Telling about ${event.getClass.getSimpleName} to the network ")
    mediator.tell(Publish(queryDB.topic, event), ActorRef.noSender)
  }
}
