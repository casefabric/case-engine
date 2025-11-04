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

package org.cafienne.actormodel

import org.cafienne.model.cmmn.instance.Path
import org.cafienne.infrastructure.serialization.JacksonSerializable
import org.cafienne.util.json._

case class ActorMetadata(actorType: ActorType, actorId: String, parent: ActorMetadata = null) extends JacksonSerializable with CafienneJson {
  override def toValue: Value[_] = new StringValue(path)

  def path: String = {
    if (hasParent) {
      s"${parent.path}/$actorType[$actorId]"
    } else {
      s"$actorType[$actorId]"
    }
  }

  val hasParent: Boolean = parent != null

  val isRoot: Boolean = !hasParent

  def root: ActorMetadata = {
    if (hasParent) {
      parent.root
    } else {
      this
    }
  }

  val parentId: String = if (hasParent) parent.actorId else ""

  override def toString: String = path

  lazy val description: String = s"$actorType[$actorId]"

  def processMember(processId: String): ActorMetadata = member(processId, ActorType.Process)

  def caseMember(caseId: String): ActorMetadata = member(caseId, ActorType.Case)

  def groupMember(groupId: String): ActorMetadata = member(groupId, ActorType.Group)

  private def member(memberId: String, memberType: ActorType): ActorMetadata = this.copy(actorType = memberType, actorId = memberId, parent = this)
}

object ActorMetadata {
  def parsePath(path: String): ActorMetadata = {
    def parsePathElement(element: String): ActorMetadata = {
      val openingBracket = element.indexOf("[")
      val closingBracket = element.indexOf("]")
      val actorType = ActorType.getEnum(element.substring(0, openingBracket))
      val actorId = element.substring(openingBracket + 1, closingBracket)
      ActorMetadata(actorType, actorId)
    }
    Path.convertRawPath(path, true).map(parsePathElement).scan(null)((parent, next) => next.copy(parent = parent)).drop(1).reverse.head
  }

}
