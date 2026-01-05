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

package org.cafienne.service.storage.actormodel

import com.typesafe.scalalogging.LazyLogging
import org.apache.pekko.actor.{Actor, ActorRef, Props, Terminated}
import org.cafienne.actormodel.ActorMetadata
import org.cafienne.actormodel.message.response.ActorTerminated
import org.cafienne.system.CaseSystem

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait StorageActorSupervisor extends Actor with LazyLogging {
  val caseSystem: CaseSystem
  protected val childActors: mutable.Map[String, ActorRef] = new mutable.HashMap[String, ActorRef]()

  def getActorRef(metadata: ActorMetadata, props: Props): ActorRef = getActorRef(metadata.actorId, props)

  def getActorRef(actorId: String, props: Props): ActorRef = {
    childActors.getOrElseUpdate(actorId, {
      // Note: we create the ModelActor as a child to our context
      val ref = context.actorOf(props, actorId)
      // Also start watching the lifecycle of the model actor
      context.watch(ref)
      ref
    })
  }

  def removeActorRef(message: Terminated): Unit = removeActorRef(message.actor.path.name)

  def removeActorRef(message: ActorTerminated): Unit = removeActorRef(message.metadata.actorId)

  private def removeActorRef(actorId: String): Unit = {
    childActors.remove(actorId)
    logger.whenDebugEnabled(logger.debug(s"Actor $actorId is removed from memory"))
  }

  /**
    * Tell Cafienne Gateway to remove the actual ModelActor with the same actor id from memory.
    * Upon successful termination, the followup action will be triggered.
    */
  def terminateModelActor(metadata: ActorMetadata, followUpAction: => Unit = {}): Unit = {
    caseSystem.engine.awaitTermination(metadata).onComplete{
      case Success(_) => followUpAction
      case Failure(exception) => logger.warn(s"Failure upon termination of model actor $metadata", exception)
    }
  }
}
