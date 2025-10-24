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

package org.cafienne.storage.archival

import com.typesafe.scalalogging.LazyLogging
import org.apache.pekko.actor.Actor
import org.cafienne.actormodel.ActorMetadata
import org.cafienne.storage.StorageUser
import org.cafienne.storage.actormodel.RootStorageActor
import org.cafienne.storage.archival.event.ArchivalRequested
import org.cafienne.system.CaseSystem

class RootArchiver(val caseSystem: CaseSystem, val user: StorageUser, val metadata: ActorMetadata) extends RootStorageActor[ArchiveNode] with LazyLogging {
  override def createInitialEvent: ArchivalRequested = ArchivalRequested(user, metadata)

  override def storageActorType: Class[_ <: Actor] = classOf[ActorDataArchiver]

  override def createOffspringNode(nodeMetadata: ActorMetadata): ArchiveNode = {
    if (nodeMetadata == metadata) {
      new RootArchiveNode(user, nodeMetadata, this)
    } else {
      ArchiveNode(user, nodeMetadata, this)
    }
  }
}
