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

package org.cafienne.system.router.cluster

import com.typesafe.scalalogging.LazyLogging
import org.apache.pekko.actor.ActorRef
import org.apache.pekko.cluster.sharding.ClusterSharding
import org.apache.pekko.util.Timeout
import org.cafienne.actormodel.message.command.{ModelCommand, TerminateModelActor}
import org.cafienne.actormodel.message.response.{ActorTerminated, CommandFailure, ModelResponse}
import org.cafienne.actormodel.{ActorMetadata, ActorType}
import org.cafienne.system.{CaseEngineGateway, CaseSystem}

import scala.concurrent.Future

class ClusterGateway(val caseSystem: CaseSystem) extends CaseEngineGateway with LazyLogging {
  val cluster = ClusterSharding(caseSystem.system)
  // Start the shard regions
  private val regions = ActorType.values().filter(!_.isGeneric).map(actorType => (actorType, new ModelActorShardRegion(this, actorType).start())).toMap

  def request(message: ModelCommand): Future[ModelResponse] = {
    import org.apache.pekko.pattern.ask
    implicit val timeout: Timeout = caseSystem.config.actor.askTimout

    regions.get(message.actorType).map(actorRef => actorRef.ask(message).asInstanceOf[Future[ModelResponse]]).getOrElse({
      logger.error(s"MAJOR ERROR: ClusterGateway has no router for actors of type '${message.actorType()}' to handle message ${message.getClass.getSimpleName}")
      Future.successful(new CommandFailure(message, new IllegalArgumentException(s"Case Engine cannot handle messages of type ${message.getClass.getSimpleName}")))
    })
  }

  def inform(message: ModelCommand, replyTo: ActorRef = ActorRef.noSender): Unit = {
    regions.get(message.actorType).map(_.tell(message, replyTo)).getOrElse(logger.error(s"Cluster: No router found for message $message"))
  }

  override def terminate(metadata: ActorMetadata): Unit = {
    val message = TerminateModelActor(metadata)
    regions.get(metadata.actorType).map(_.tell(message, ActorRef.noSender)).getOrElse(logger.error(s"Cluster: No router found to terminate ${metadata.actorId} of type ${metadata.actorType.actorClass.getName}"))
  }

  override def awaitTermination(metadata: ActorMetadata): Future[ActorTerminated] = {
    import org.apache.pekko.pattern.ask
    implicit val timeout: Timeout = caseSystem.config.actor.askTimout
    val message = TerminateModelActor(metadata)

    regions.get(metadata.actorType).map(actorRef => actorRef.ask(message).asInstanceOf[Future[ActorTerminated]]).getOrElse({
      logger.error(s"MAJOR ERROR: ClusterGateway has no router for actors of type '${metadata.actorType}' to handle message ${message.getClass.getSimpleName}")
      Future.successful(new IllegalArgumentException(s"Case Engine cannot handle messages of type ${message.getClass.getSimpleName}"))
      Future.successful(ActorTerminated(metadata))
    })
  }

}
