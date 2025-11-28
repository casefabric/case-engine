package com.casefabric.ai

import com.embabel.agent.config.annotation.{EnableAgents, McpServers}
import org.springframework.ai.mcp.client.common.autoconfigure.StdioTransportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties

@SpringBootApplication(exclude = Array(classOf[CassandraAutoConfiguration], classOf[StdioTransportAutoConfiguration]))
@ConfigurationProperties("application.yml")
@EnableAgents(mcpServers = Array(McpServers.DOCKER_DESKTOP))
class AiAgentSystem {
}