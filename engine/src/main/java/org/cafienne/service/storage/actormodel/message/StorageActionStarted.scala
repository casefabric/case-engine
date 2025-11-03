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

package org.cafienne.service.storage.actormodel.message

import com.fasterxml.jackson.core.JsonGenerator
import org.cafienne.actormodel.ActorMetadata
import org.cafienne.infrastructure.serialization.Fields
import org.cafienne.util.json.ValueList

trait StorageActionStarted extends StorageEvent {
  val children: Seq[ActorMetadata]

  override def write(generator: JsonGenerator): Unit = {
    super.writeStorageEvent(generator)
    writeField(generator, Fields.children, children.map(_.toString))
  }
}

object StorageActionStarted {
  def deserializeChildren(metadata: ActorMetadata, jsonList: ValueList): Seq[ActorMetadata] = {
    import scala.jdk.CollectionConverters.CollectionHasAsScala
    jsonList.getValue.asScala.map(_.getValue.toString).map(s => ActorMetadata.parsePath(s)).map(_.copy(parent = metadata)).toSeq
  }
}
