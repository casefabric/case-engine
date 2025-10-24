package org.cafienne.storage.actormodel.command

import org.cafienne.actormodel.ActorMetadata
import org.cafienne.infrastructure.serialization.JacksonSerializable
import org.cafienne.storage.StorageUser

case class ClearTimerData(user: StorageUser, metadata: ActorMetadata) extends JacksonSerializable {
}
