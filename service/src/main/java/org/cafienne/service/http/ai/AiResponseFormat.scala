package org.cafienne.service.http.ai

import com.embabel.agent.core.{DomainTypePropertyDefinition, PropertyDefinition, SimplePropertyDefinition}
import io.swagger.v3.oas.annotations.media.Schema
import org.cafienne.util.json.{CafienneJson, JSONReader, StringValue, Value, ValueList, ValueMap}
import com.github.victools.jsonschema.generator.OptionPreset
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion

import scala.annotation.meta.field


object AiResponseFormat {

  //implicit val aiAgentResponseReader: EntityReader[AiAgentResponse] = entityReader[AiAgentResponse]

  @Schema(description = "Ai Agent response format")
  case class AiAgentResponse(
                              @(Schema @field)(description = "Name of the agent")
                              name: String,
                              @(Schema @field)(description = "Input Fields")
                              inputFields: Set[String],
                              @(Schema @field)(description = "Output Fields")
                              outputField: String) extends CafienneJson {

    override def toValue: Value[_] = {
      val root = new ValueMap()
      val inputFields = new ValueList()

      this.inputFields.foreach(field => inputFields.add( new StringValue(field)))
      root.put("inputFields", inputFields)
      root.put("outputField", new StringValue(outputField))
      root.put("name", new StringValue(this.name))
      root
    }
  }

  @Schema(description = "Ai Domain types response format")
  case class AiTypeResponse(
                              @(Schema @field)(description = "Name of the domain type")
                              name: String,
                              @(Schema @field)(description = "Properties of the domain type")
                              properties: List[PropertyDefinition]) extends CafienneJson {

    override def toValue: Value[_] = {
      val root = new ValueMap()
      val props = new ValueList()
      root.put("name", new StringValue(name))
      properties.map(pd => {
        val prop = new ValueMap();
        prop.put("name", new StringValue(pd.getName))
        prop.put("cardinality", new StringValue(pd.getCardinality.toString))
        prop.put("description", new StringValue(pd.getDescription))
        pd.getClass.getSimpleName match {
          case "SimplePropertyDefinition" =>
            prop.put("class", new StringValue("string"))
          case pdm@"DomainTypePropertyDefinition" =>
            prop.put("class", new StringValue(pd.asInstanceOf[DomainTypePropertyDefinition].getType.getName))
        }
        props.add(prop)
      })
      root.put("properties", props)
      root
    }
  }

  private def toJsonSchema(field: String) = {
    val root = new ValueMap()
    root.put("name", new StringValue(field))
    val fieldClass = Class.forName(field)
    val configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
    val config = configBuilder.build
    val generator = new SchemaGenerator(config)
    val jsonSchema = generator.generateSchema(fieldClass)
    root.put("schema", JSONReader.parse(jsonSchema.toPrettyString))
    root
  }

}
