package org.cafienne.persistence.infrastructure.lastmodified.header

import org.cafienne.persistence.infrastructure.lastmodified.LastModifiedRegistration
import org.cafienne.persistence.querydb.schema.QueryDB

case class TenantLastModifiedHeader(override val value: Option[String], override val queryDB: QueryDB) extends LastModifiedHeader {
  override val name: String = Headers.TENANT_LAST_MODIFIED
  override val registration: LastModifiedRegistration = queryDB.tenantLastModifiedRegistration
}
