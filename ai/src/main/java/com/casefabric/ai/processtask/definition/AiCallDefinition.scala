
package com.casefabric.ai.processtask.definition

import com.casefabric.ai.processtask.AiCall
import org.cafienne.model.cmmn.definition.{CMMNElementDefinition, ModelDefinition}
import org.cafienne.model.processtask.definition.SubProcessDefinition
import org.cafienne.model.processtask.instance.ProcessTaskActor
import org.w3c.dom.Element

import java.util

object AiCallDefinition {
  // Raw, hard coded output parameter names
  val RESPONSE_PAYLOAD_PARAMETER = "responsePayload"
  val RESPONSE_CODE_PARAMETER = "responseCode"
  val RESPONSE_MESSAGE_PARAMETER = "responseMessage"
}

class AiCallDefinition(element: Element, processDefinition: ModelDefinition, parentElement: CMMNElementDefinition) extends SubProcessDefinition(element, processDefinition, parentElement) {

  val prompt: PromptDefinition = parse("prompt", classOf[PromptDefinition], true)
  val response: ResponseDefinition = parse("response", classOf[ResponseDefinition], true)

  override def getRawOutputParameterNames: util.Set[String] = {
    val pNames = super.getExceptionParameterNames
    pNames.add(AiCallDefinition.RESPONSE_CODE_PARAMETER)
    pNames.add(AiCallDefinition.RESPONSE_MESSAGE_PARAMETER)
    pNames.add(AiCallDefinition.RESPONSE_PAYLOAD_PARAMETER)
    pNames
  }

  override def createInstance(processTaskActor: ProcessTaskActor) = new AiCall(processTaskActor, this)

  override def equalsWith(`object`: AnyRef): Boolean = equalsWith(`object`, this.sameAiCall)

  private def sameAiCall(other: AiCallDefinition): Boolean = sameSubProcess(other)
}