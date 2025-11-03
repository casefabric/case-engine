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

package org.cafienne.service.storage.deletion

import org.cafienne.actormodel.ActorMetadata
import org.cafienne.service.storage.StorageUser
import org.cafienne.service.storage.actormodel.OffspringNode
import org.cafienne.service.storage.deletion.command.RemoveActorData

case class RemoveNode(user: StorageUser, metadata: ActorMetadata, actor: RootRemover) extends OffspringNode {
  override def createStorageCommand: Any = RemoveActorData(user, metadata)
}
