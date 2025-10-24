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

package org.cafienne.storage.restore

import org.cafienne.actormodel.ActorMetadata
import org.cafienne.storage.StorageUser
import org.cafienne.storage.actormodel.OffspringNode
import org.cafienne.storage.archival.Archive
import org.cafienne.storage.restore.command.RestoreArchive

case class RestoreNode(user: StorageUser, metadata: ActorMetadata, actor: RootRestorer) extends OffspringNode {
  override def createStorageCommand: Any = RestoreArchive(user, metadata, archive)
  var archive: Archive = _

  private def parentCompleted: Boolean = actor.getParent(this).fold(true)(_.hasCompleted)

  override def continueStorageProcess(): Unit = {
    if (parentCompleted) {
      startStorageProcess()
    }
  }
}
