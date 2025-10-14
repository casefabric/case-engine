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

package org.cafienne.system

import org.apache.pekko.actor.{Actor, ActorRef}
import org.cafienne.actormodel.ActorMetadata
import org.cafienne.actormodel.message.command.ModelCommand
import org.cafienne.actormodel.message.response.{ActorTerminated, ModelResponse}
import org.cafienne.system.router.singleton.SingletonGateway

import scala.concurrent.Future

trait CaseEngineGateway {
  def request(message: ModelCommand): Future[ModelResponse]

  def inform(message: ModelCommand, sender: ActorRef = Actor.noSender): Unit

  def terminate(metadata: ActorMetadata): Unit

  def awaitTermination(metadata: ActorMetadata): Future[ActorTerminated]
}

object CaseEngineGateway {
  def createGateway(caseSystem: CaseSystem): CaseEngineGateway = {
    new SingletonGateway(caseSystem)
  }
}
