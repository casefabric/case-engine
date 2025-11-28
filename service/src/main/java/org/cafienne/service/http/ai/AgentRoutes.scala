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

package org.cafienne.service.http.ai

import io.swagger.v3.oas.annotations.media.{ArraySchema, Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.Operation
import jakarta.ws.rs._
import org.apache.pekko.http.scaladsl.server.Route
import org.cafienne.service.http.CaseEngineHttpServer

import scala.jdk.CollectionConverters._

@Path("/ai")
class AgentRoutes(override val httpService: CaseEngineHttpServer) extends AiRoute {
  import org.cafienne.service.http.ai.AiResponseFormat._
  override def routes: Route = concat(getAllAgents)

  @Path("/agents")
  @GET
  @Operation(
    summary = "Get available agents",
    description = "Get a list of agents and the available parameters",
    tags = Array("ai"),
    responses = Array(
      new ApiResponse(description = "Agents found", responseCode = "200", content = Array(new Content(array = new ArraySchema(schema = new Schema(implementation = classOf[AiAgentResponse]))))),
      new ApiResponse(description = "No agents found", responseCode = "404")
    )
  )
  @Produces(Array("application/json"))
  def getAllAgents: Route = get {
    path("agents") {
        val answer = agentPlatform().agents().asScala.flatMap(agent => agent.getGoals.asScala.map { goal =>
          AiAgentResponse(goal.getExport.getName,
            goal.getExport.getStartingInputTypes.asScala.map(input => input.getName).toSet,
            goal.getOutputType.getName)
        })
        completeJson(answer.toSeq)
      }
  }

}
