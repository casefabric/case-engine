package org.cafienne.actormodel.communication

import com.fasterxml.jackson.core.JsonGenerator
import org.cafienne.actormodel.message.UserMessage
import org.cafienne.actormodel.{ActorMetadata, ActorType}
import org.cafienne.infrastructure.serialization.Fields
import org.cafienne.util.json.ValueMap

trait CaseSystemCommunicationMessage extends UserMessage {

  def readSender(json: ValueMap, compatibilityFieldName: Fields): ActorMetadata = {
    if (json.has(Fields.sender)) {
      json.readMetadata(Fields.sender)
    } else {
      // Compatibility on older JSON format.
      // Note that in using this event only the ActorId is used (to fetch state). So no need to determine rest of the metadata.
      val actorId = json.readString(compatibilityFieldName)
      new ActorMetadata(ActorType.ModelActor, actorId)
    }
  }

  def writeSender(generator: JsonGenerator, sender: ActorMetadata): Unit = {
    writeField(generator, Fields.sender, sender)
  }

  def readReceiver(json: ValueMap): ActorMetadata = {
    if (json.has(Fields.receiver)) {
      json.readMetadata(Fields.receiver)
    } else {
      // Compatibility on older JSON format.
      // Note that in using this event only the ActorId is used (to fetch state). So no need to determine rest of the metadata.
      val actorId = json.readString(Fields.targetActorId)
      new ActorMetadata(ActorType.ModelActor, actorId)
    }
  }

  def writeReceiver(generator: JsonGenerator, receiver: ActorMetadata): Unit = {
    writeField(generator, Fields.receiver, receiver)
  }

  override def toString: String = getDescription
}
