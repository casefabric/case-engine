
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

import org.apache.pekko.actor.{ActorRef, PoisonPill, Props}
import org.apache.pekko.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import org.apache.pekko.util.Timeout
import org.cafienne.service.storage.StorageCoordinator
import org.cafienne.service.storage.actormodel.command.{ClearTimerData, StorageCommand}
import org.cafienne.service.storage.actormodel.message.StorageMessage
import org.cafienne.service.timerservice.TimerService

import scala.concurrent.Future

class CaseServiceGateway(caseSystem: CaseSystem) {
  // Create singleton actors
  private val timerService: ActorRef = createSingletonReference(classOf[TimerService], TimerService.IDENTIFIER)
  private val storageCoordinator: ActorRef = createSingletonReference(classOf[StorageCoordinator], StorageCoordinator.IDENTIFIER)

  def askStorageCoordinator(command: StorageCommand): Future[StorageMessage] = {
    import org.apache.pekko.pattern.ask
    implicit val timeout: Timeout = caseSystem.config.actor.askTimout

    storageCoordinator.ask(command).asInstanceOf[Future[StorageMessage]]
  }

  def informTimerService(message: ClearTimerData, sender: ActorRef): Unit = {
    timerService.tell(message, sender)
  }

  def createSingletonReference(actorClass: Class[_], identifier: String): ActorRef = {
    if (caseSystem.hasClusteredConfiguration) {
      //TODO handle shutdown of service (now PoisonPill is sent to the service, but it is not handled)
      caseSystem.system.actorOf(
        ClusterSingletonManager.props(
          singletonProps = Props(actorClass, this.caseSystem),
          terminationMessage = PoisonPill,
          settings = ClusterSingletonManagerSettings(caseSystem.system)),
        name = s"$identifier")
      //return access to the singleton actor
      caseSystem.system.actorOf(
        ClusterSingletonProxy.props(
          singletonManagerPath = s"/user/$identifier",
          settings = ClusterSingletonProxySettings(caseSystem.system)),
        name = s"$identifier-proxy")
    } else {
      caseSystem.system.actorOf(Props.create(actorClass, caseSystem), identifier)
    }
  }
}
