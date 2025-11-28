package com.casefabric.ai.agent.dynamic

import com.embabel.agent.core.AgentPlatform
import org.springframework.beans.factory.annotation.Autowired

class DeploymentSupport() {

  @Autowired
  private var agentPlatform: AgentPlatform = null

  def createDynamicType() = {
    //val newDynamicType = new DynamicType("MyDynamicType", "My Dynamic Description", util.List.empty[_ <: PropertyDefinition], List.empty, "")
    //  .withProperty(new SimplePropertyDefinition("nameProp", "string", Cardinality.ONE, "nameProp Description"))
    //agentPlatform.getDomainTypes.add(newDynamicType)
  }
}
