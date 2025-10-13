package org.cafienne.service.storage.actormodel.command

import org.cafienne.actormodel.ActorMetadata
import org.cafienne.infrastructure.serialization.JacksonSerializable
import org.cafienne.service.storage.StorageUser

case class ClearTimerData(user: StorageUser, metadata: ActorMetadata) extends JacksonSerializable {
}
