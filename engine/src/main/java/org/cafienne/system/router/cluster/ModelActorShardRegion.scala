package org.cafienne.system.router.cluster

import org.apache.pekko.actor.{ActorRef, ActorSystem, Props}
import org.apache.pekko.cluster.sharding.{ClusterSharding, ShardRegion}
import org.cafienne.actormodel.message.command.{ModelCommand, TerminateModelActor}
import org.cafienne.actormodel.{ActorMetadata, ActorType}

class ModelActorShardRegion(val gateway: ClusterGateway, val actorType: ActorType) {
  private val system: ActorSystem = gateway.caseSystem.system
  private val cluster: ClusterSharding = gateway.cluster
  private val modelActorProps: Props = Props(actorType.actorClass, gateway.caseSystem)

  //pekko.cluster.sharding.number-of-shards
  private val numberOfPartitions = system.settings.config.getInt("pekko.cluster.sharding.number-of-shards")

  private val idExtractor: ShardRegion.ExtractEntityId = {
    case envelope: ModelCommand => (envelope.target().path, envelope)
    case msg: TerminateModelActor => (msg.metadata.path, msg)
    case other => throw new Error(s"Cannot extract actor id for messages of type ${other.getClass.getName}")
  }

  private def calculateShard(metadata: ActorMetadata): String = {
    val pidHashKey: Long = metadata.root.actorId.hashCode
    (pidHashKey % numberOfPartitions).toString
  }

  // Subcases and process tasks need to end up at the same shard as the original case. (also in followup calls)
  //use the rootCaseId for deciding on the shard. This is added to the CaseMembership
  private val shardResolver: ShardRegion.ExtractShardId = {
    case envelope: ModelCommand => calculateShard(envelope.target())
    case tma: TerminateModelActor => calculateShard(tma.metadata)
    case other => throw new Error(s"Cannot resolve shard for messages of type ${other.getClass.getName}")
  }

  def start(): ActorRef = {
    cluster.start(typeName = actorType.description, entityProps = modelActorProps, extractEntityId = idExtractor, extractShardId = shardResolver)
  }
}
