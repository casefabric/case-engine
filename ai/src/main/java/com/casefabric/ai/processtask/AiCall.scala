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

import com.casefabric.ai.AiProvider
import com.casefabric.ai.processtask.definition.AiCallDefinition
import com.casefabric.ai.util.JacksonValueHelper
import com.embabel.agent.api.common.autonomy.AgentInvocation
import com.embabel.agent.domain.io.UserInput
import org.cafienne.model.processtask.implementation.SubProcess
import org.cafienne.model.processtask.instance.ProcessTaskActor
import org.slf4j.LoggerFactory

import java.util.concurrent.CompletableFuture

class AiCall(processTask: ProcessTaskActor, definition: AiCallDefinition) extends SubProcess[AiCallDefinition](processTask, definition) {
  private val logger = LoggerFactory.getLogger(classOf[AiCall])

  final private val aiCallResponse = new AiCallResponse(this)

  override def reactivate(): Unit = {
    start()
  }

  override def start(): Unit = {
    val successful = runCall
    // Print debug information
    processTaskActor.addDebugInfo(() => aiCallResponse.getDebugInfo)
    // Set raw output parameters
    getRawOutputParameters.merge(aiCallResponse.toJSON)
    logger.debug("ProcessTask complete with :" + getRawOutputParameters.toString)
    if (successful) {
      raiseComplete()
    }
    else {
      setFault(aiCallResponse.getException)
      raiseFault(aiCallResponse.getErrorDescription)
    }
  }

  private def runCall = {
    // Bind any parameters in the URL, any content and the http method to the input parameters of this task.
    val prompt: String = getDefinition.prompt.resolve(processTaskActor)
    val responseClass: String = getDefinition.response.resolve(processTaskActor)

    try {
      val agentPlatform = AiProvider.getAgentPlatform
      val agentCall =
        AgentInvocation.builder(agentPlatform)
          .options(p => {
            p.contextId(this.processTask.getId) //not sure if this is really useful.
            p.verbosity(v => {
              v.showPrompts(true)
              v.showLlmResponses(true)
              v.debug(true)
          })}).build(Class.forName(responseClass))
      val fut: CompletableFuture[_] = agentCall.invokeAsync(new UserInput(prompt))
      val answer= fut.get()
      logger.debug(s"Agent invocation answer: $answer")
      val asJson = JacksonValueHelper.toValue(answer)
//      val ai = AiProvider.getAi
//      val answer = ai.withLlm("mistral:latest")
//        .withTools(Array(CoreToolGroups.WEB, CoreToolGroups.BROWSER_AUTOMATION): _*)
//        .generateText(prompt)
      processTask.addDebugInfo(() => "Call Ai with prompt: " + prompt + " and answer: " + answer.toString)

      //val answer = agentCall.invoke("Create a Story about EV and popular car brands")
      aiCallResponse.setResponseCode(200)
      aiCallResponse.setResponseMessage(answer.toString)
      aiCallResponse.setResponsePayload(asJson)
      true
    } catch {
      case ex: Exception =>
        logger.error(s"AI call $prompt failed due to ${ex.getMessage} " + Option(ex.getCause).map(cause => "cause: " + cause.getMessage).getOrElse(""), ex)
        aiCallResponse.setResponseMessage(s"Ai call $prompt failed due to ${ex.getMessage} " + Option(ex.getCause).map(cause => "cause: " +cause.getMessage).getOrElse(""))
        aiCallResponse.handleFailure(s"Ai call $prompt failed", ex)
    }
  }

  override def suspend(): Unit = {
  }

  override def terminate(): Unit = {
  }

  override def resume(): Unit = {
  }
}