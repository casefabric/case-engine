package org.cafienne.persistence.querydb.materializer

import org.apache.pekko.persistence.query.Offset
import org.cafienne.persistence.querydb.materializer.cases.TestCaseStorageTransaction
import org.cafienne.persistence.querydb.materializer.consentgroup.ConsentGroupStorageTransaction
import org.cafienne.persistence.querydb.materializer.tenant.TenantStorageTransaction
import org.cafienne.persistence.querydb.record.CaseRecord

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

object TestQueryDB extends QueryDBStorage {

  // Some day: extend this with tests for TenantStorage and ConsentGroupStorage

  val transactions: ListBuffer[TestCaseStorageTransaction] = new ListBuffer[TestCaseStorageTransaction]()

  def latest: TestCaseStorageTransaction = transactions.last

  private def findTransaction(persistenceId: String) = {
    transactions.find(t => t.records.exists(r => r.isInstanceOf[CaseRecord] && r.asInstanceOf[CaseRecord].id == persistenceId))
  }

  def hasTransaction(persistenceId: String): Boolean = findTransaction(persistenceId).nonEmpty

  def getTransaction(persistenceId: String): TestQueryDBTransaction = {
    // println(s"Searching for transaction on case $persistenceId in ${transactions.size} transactions, with id's ${transactions.map(_.persistenceId)}")
    findTransaction(persistenceId).getOrElse(throw new Exception(s"Cannot find a transaction for case $persistenceId"))
  }

  override def createCaseTransaction(): TestCaseStorageTransaction = {
    // println(s"\n\nAsking for new case transaction on persistence id $caseInstanceId\n\n")
    transactions += new TestCaseStorageTransaction()
    transactions.last
  }

  override def createConsentGroupTransaction(): ConsentGroupStorageTransaction = ???

  override def createTenantTransaction(): TenantStorageTransaction = ???

  override def getOffset(offsetName: String): Future[Offset] = Future.successful(Offset.noOffset)
}
