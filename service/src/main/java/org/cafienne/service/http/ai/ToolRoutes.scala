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

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{ArraySchema, Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.ws.rs._
import org.apache.pekko.http.scaladsl.server.Route
import org.cafienne.util.json.{StringValue, ValueList}
import org.cafienne.service.http.CaseEngineHttpServer

import scala.jdk.CollectionConverters._

@Path("/ai")
class ToolRoutes(override val httpService: CaseEngineHttpServer) extends AiRoute {
  import org.cafienne.service.http.ai.AiResponseFormat._

  override def routes: Route = concat(getAllTools)

  @Path("/tools")
  @GET
  @Operation(
    summary = "Get available tools",
    description = "Get a list of tools",
    tags = Array("ai"),
    responses = Array(
      new ApiResponse(description = "Tools found", responseCode = "200", content = Array(new Content(array = new ArraySchema(schema = new Schema(implementation = classOf[List[String]]))))),
      new ApiResponse(description = "No tools found", responseCode = "404")
    )
  )
  @Produces(Array("application/json"))
  def getAllTools: Route = get {
    path("tools") {
        val answer = agentPlatform().getToolGroupResolver.availableToolGroups().asScala.map(toolGroup => {
          new StringValue(toolGroup.getName)
        }).toList
        completeJson(new ValueList(answer))
      }
  }

}
