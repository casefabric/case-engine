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
import org.cafienne.util.json.ValueList
import org.cafienne.service.http.CaseEngineHttpServer

import scala.jdk.CollectionConverters._

@Path("/ai")
class TypeRoutes(override val httpService: CaseEngineHttpServer) extends AiRoute {
  import org.cafienne.service.http.ai.AiResponseFormat._

  override def routes: Route = concat(getAllTypes)

  @Path("/types")
  @GET
  @Operation(
    summary = "Get available domain types",
    description = "Get a list of domain types",
    tags = Array("ai"),
    responses = Array(
      new ApiResponse(description = "Domain types found", responseCode = "200", content = Array(new Content(array = new ArraySchema(schema = new Schema(implementation = classOf[AiTypeResponse]))))),
      new ApiResponse(description = "No domain types found", responseCode = "404")
    )
  )
  @Produces(Array("application/json"))
  def getAllTypes: Route = get {
    path("types") {
        val answer = agentPlatform().getDomainTypes.asScala.map(dt => {
          AiTypeResponse(dt.getName, dt.getOwnProperties.asScala.toList)
        }).toList
        completeJson(new ValueList(answer))
      }
  }

}
