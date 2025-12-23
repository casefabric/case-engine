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

import com.embabel.agent.core.{DomainType, DomainTypePropertyDefinition, PropertyDefinition, SimplePropertyDefinition}
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
      val answer = addMissingTypes()
      completeJson(new ValueList(answer))
    }
  }

  private def addMissingTypes(): Seq[AiTypeResponse] = {
    val rawTypes = agentPlatform().getDomainTypes.asScala.toList
    val foundTypes: List[AiTypeResponse] = agentPlatform().getDomainTypes.asScala.filter(domainTypeFilter).map(dt => {
      AiTypeResponse(dt.getName, dt.getProperties.asScala.filter(propFilter).toList)
    }).toList
    var missingTypes: Set[AiTypeResponse] = Set.empty
    rawTypes.foreach(dt => {
      dt.getProperties.asScala.foreach {
        case definition: DomainTypePropertyDefinition =>
          if (!rawTypes.exists(ft => ft.getName.equals(definition.getType.getName))) {
            //carve out things that should not be added to the responses
            if (domainTypeFilter(definition.getType)) {
              missingTypes += AiTypeResponse(definition.getType.getName, definition.getType.getProperties.asScala.filter(propFilter).toList)
            }
          }
        case definition: SimplePropertyDefinition => //
        case _ => //
      }
    })
    //Add the AiRequest type with prompt.
    (foundTypes ++ missingTypes).filter(dt => dt.properties.nonEmpty).toList
  }

  private def domainTypeFilter(dt: DomainType): Boolean = {
    val toMatch = dt.getName.toLowerCase
    theFilter(toMatch)
  }

  private def propFilter(pd: PropertyDefinition): Boolean = {
    val toMatch = pd.getName.toLowerCase
    theFilter(toMatch)
  }

  private def theFilter(toMatch: String): Boolean = {
    if (toMatch.contains("log") || toMatch.contains('$') || toMatch.contains("companion") || toMatch.contains("userinput")) false else true
  }

}
