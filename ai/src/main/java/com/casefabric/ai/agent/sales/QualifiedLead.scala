package com.casefabric.ai.agent.sales

import com.casefabric.ai.util.JacksonValueHelper
import com.fasterxml.jackson.annotation.JsonProperty
import org.cafienne.util.json.{CafienneJson, Value}

case class Company(@JsonProperty("name")name: String, @JsonProperty("website")website: String, @JsonProperty("products")products: String) extends CafienneJson {

  override def toValue: Value[_] = JacksonValueHelper.toValue(this)
}

//case class QualifiedLead(@JsonProperty("company")company: Company, @JsonProperty("qualification")qualification: Int) extends CafienneJson {
case class QualifiedLead(@JsonProperty("qualification")qualification: Int) extends CafienneJson {

    override def toValue: Value[_] = JacksonValueHelper.toValue(this);
}

