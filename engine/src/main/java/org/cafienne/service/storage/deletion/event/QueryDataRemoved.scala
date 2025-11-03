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

package org.cafienne.service.storage.deletion.event

import org.cafienne.actormodel.ActorMetadata
import org.cafienne.infrastructure.serialization.{Fields, Manifest}
import org.cafienne.service.storage.StorageUser
import org.cafienne.service.storage.actormodel.event.QueryDataCleared
import org.cafienne.util.json.ValueMap

@Manifest
case class QueryDataRemoved(user: StorageUser, metadata: ActorMetadata, override val optionalJson: Option[ValueMap] = None) extends RemovalEvent with QueryDataCleared

object QueryDataRemoved {
  def deserialize(json: ValueMap): QueryDataRemoved = QueryDataRemoved(StorageUser.deserialize(json), json.readMetadata(Fields.metadata), Some(json))
}
