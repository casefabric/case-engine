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

package org.cafienne.storage.deletion

import com.typesafe.scalalogging.LazyLogging
import org.apache.pekko.actor.Actor
import org.cafienne.storage.StorageUser
import org.cafienne.storage.actormodel.{ActorMetadata, RootStorageActor}
import org.cafienne.storage.deletion.event.RemovalRequested
import org.cafienne.system.CaseSystem

class RootRemover(val caseSystem: CaseSystem, val user: StorageUser, val metadata: ActorMetadata) extends RootStorageActor[RemoveNode] with LazyLogging {
  override def createInitialEvent: RemovalRequested = RemovalRequested(user, metadata)

  override def storageActorType: Class[_ <: Actor] = classOf[ActorDataRemover]

  override def createOffspringNode(metadata: ActorMetadata): RemoveNode = RemoveNode(user, metadata, this)
}
