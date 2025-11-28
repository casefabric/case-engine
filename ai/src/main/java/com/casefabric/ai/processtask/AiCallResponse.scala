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
package com.casefabric.ai.processtask

import com.casefabric.ai.processtask.definition.AiCallDefinition
import org.cafienne.util.json.{LongValue, StringValue, Value, ValueMap}

class AiCallResponse private[processtask](private val call: AiCall) {
  private var responseCode = -1
  private var responseMessage = ""
  private var responsePayload: Value[_] = new StringValue("")

  private[processtask] def setResponseCode(responseCode: Int): Unit = {
    this.responseCode = responseCode
  }

  private[processtask] def setResponseMessage(responseMessage: String): Unit = {
    this.responseMessage = responseMessage
  }

  private[processtask] def setResponsePayload(responsePayload :Value[_]): Unit = {
    this.responsePayload = responsePayload
  }

  private[processtask] var errorDescription = ""
  private[processtask] var cause: Throwable = null

  private[processtask] def handleFailure(description: String, cause: Throwable): Boolean = {
    this.cause = cause
    handleFailure(description)
  }

  private[processtask] def handleFailure(description: String) = {
    this.errorDescription = description
    false
  }

  private[processtask] def getErrorDescription = errorDescription

  private[processtask] def getException = if (cause != null) Value.convert(cause)
  else new ValueMap("description", getErrorDescription, "response", getResponseDebugInfo)

  private[processtask] def toJSON = {
    val responseJson = new ValueMap
    responseJson.put(AiCallDefinition.RESPONSE_CODE_PARAMETER, new LongValue(responseCode))
    responseJson.put(AiCallDefinition.RESPONSE_MESSAGE_PARAMETER, new StringValue(responseMessage))
    responseJson.put(AiCallDefinition.RESPONSE_PAYLOAD_PARAMETER, responsePayload)
    responseJson
  }

  private[processtask] def getDebugInfo = new ValueMap("Request", getRequestDebugInfo, "Response", getResponseDebugInfo)

  private[processtask] def getRequestDebugInfo = {
    val requestDebugInfo = new ValueMap
//    requestDebugInfo.put("microflow", new StringValue(microflow))
//    if (params != null) requestDebugInfo.put("payload", params)
    requestDebugInfo
  }

  private[processtask] def getResponseDebugInfo = {
    val headersConcatenated = new ValueMap // Convert header values to concatenated spaced string for ease of reading
    val responseDebugInfo = new ValueMap("code", responseCode, "message", new StringValue(responseMessage), "headers", headersConcatenated, "content", responsePayload)
    if (responseCode == -1 && errorDescription.nonEmpty) responseDebugInfo.put("error", new StringValue(errorDescription))
    responseDebugInfo
  }

}