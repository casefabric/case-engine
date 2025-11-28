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
import jakarta.ws.rs._
import org.apache.pekko.http.scaladsl.server.Route
import org.cafienne.util.json.ValueMap
import org.cafienne.service.http.CaseEngineHttpServer

import scala.jdk.CollectionConverters._

@Path("/ai")
class LlmRoutes(override val httpService: CaseEngineHttpServer) extends AiRoute {
  import org.cafienne.service.http.ai.AiResponseFormat._
  override def routes: Route = concat(getAllAgents)

  @Path("/llm")
  @GET
  @Operation(
    summary = "Get available Llms",
    description = "Get a list of Llms and the available parameters",
    tags = Array("ai"),
    responses = Array(
      new ApiResponse(description = "Llms found", responseCode = "200", content = Array(new Content(array = new ArraySchema(schema = new Schema(implementation = classOf[AiAgentResponse]))))),
      new ApiResponse(description = "No Llm found", responseCode = "404")
    )
  )
  @Produces(Array("application/json"))
  def getAllAgents: Route = get {
    path("llm") {
        completeJson(new ValueMap())
    }
  }

}
