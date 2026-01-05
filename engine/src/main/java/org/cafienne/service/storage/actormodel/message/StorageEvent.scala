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
import org.cafienne.actormodel.message.event.ModelEvent
import org.cafienne.actormodel.{ActorMetadata, ActorType, ModelActor}
import org.cafienne.infrastructure.serialization.Fields
import org.cafienne.service.storage.StorageUser
import org.cafienne.util.json.{CafienneJson, Value, ValueMap}

import java.time.Instant

/** Provide a scala base class that can be used as alternative for ModelEvent,
  * in order to generate events from ModelActor Data Removal pattern and have those events end up also in Cafienne Debugger.
  */
trait StorageEvent extends StorageMessage with CafienneJson with ModelEvent {
  override val metadata: ActorMetadata
  val user: StorageUser
  val tenant: String = user.tenant
  override def getCorrelationId: String = ""
  override val actorId: String = metadata.actorId
  override val actorType: ActorType = metadata.actorType
  val optionalJson: Option[ValueMap] = None

  private val json = optionalJson.getOrElse(
    new ValueMap(Fields.modelEvent, asModelEvent(), Fields.user, user, Fields.actorId, actorId, Fields.tenant, tenant)
  )

  override def toValue: Value[_] = json

  def writeStorageEvent(generator: JsonGenerator): Unit = {
    writeField(generator, Fields.modelEvent, json.readMap(Fields.modelEvent))
    writeField(generator, Fields.metadata, metadata)
  }

  override def write(generator: JsonGenerator): Unit = {
    writeStorageEvent(generator)
  }

  def asModelEvent(): ValueMap = {
    new ValueMap(
      Fields.actorId, actorId,
      Fields.tenant, tenant,
      Fields.timestamp, Instant.now,
      Fields.user, user,
    )
  }

  override def updateActorState(actor: ModelActor): Unit = {}

  override def getTimestamp: Instant = json.readMap(Fields.modelEvent).readInstant(Fields.timestamp)

  override def getDescription: String = getClass.getSimpleName

  override def rawJson(): ValueMap = json

  override def getUser: StorageUser = user

  override def getActorId: String = actorId
}

object StorageEvent {
  val TAG: String = "cafienne:storage"
}
