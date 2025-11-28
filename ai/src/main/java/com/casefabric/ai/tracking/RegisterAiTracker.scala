package com.casefabric.ai.tracking

import com.embabel.agent.core.AgentPlatform
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component object RegisterAiTracker {
  private val LOG = LoggerFactory.getLogger(classOf[RegisterAiTracker])
}

@Component
class RegisterAiTracker {
  import scala.jdk.CollectionConverters._

  private val logger = LoggerFactory.getLogger(classOf[RegisterAiTracker])

  @Autowired
  private[tracking] var agentPlatform: AgentPlatform = null

  @EventListener def onApplicationEvent(event: ContextRefreshedEvent): Unit = {
    val bla = agentPlatform.agents().asScala
    bla.foreach(agent => {
      logger.info("Agent {}\n Domain types: {}\n Dynamic types: {}\n Jvm types: {}", agent.getName, agent.getDomainTypes.asScala.map(d => d.getName).mkString(","), agent.getDynamicTypes.asScala.map(d => d.getName).mkString(","), agent.getJvmTypes.asScala.map(j => j.getName).mkString(","))
    })
  }
}