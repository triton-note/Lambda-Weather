package org.fathens.triton_note.lambda.report2catches

import scala.collection.JavaConversions._

import org.fathens.triton_note.lambda.Logger
import org.fathens.triton_note.lambda.dynamodb.{ DMap, DynamoDB, DynamoEvent, TABLE_NAME, table }
import org.fathens.triton_note.lambda.report2catches.model.{ Catches, Report }

import com.amazonaws.services.dynamodbv2.document.{ PrimaryKey, TableWriteItems }
import com.amazonaws.services.lambda.runtime.Context

object Main {
  def handler(event: DMap, context: Context) {
    val logger = new Logger(context.getLogger);
    logger.info(f"Passed records: ${event}");

    def insert(report: Report) {
      report.toFishes.foreach { fish =>
        val outcome = table.putItem(fish.asItem)
        logger.info(f"Put item result: ${outcome.getPutItemResult}")
      }
    }
    def remove(report: Report) {
      val keys = Catches.makePrimaryKeys(report)
      val outcome = DynamoDB.delegate.batchWriteItem(new TableWriteItems(TABLE_NAME).withPrimaryKeysToDelete(keys: _*))
      logger.info(f"Delete items result: ${outcome.getBatchWriteItemResult}: ${outcome.getUnprocessedItems}")
    }
    def modify(oldReport: Report, newReport: Report) {
      remove(oldReport)
      insert(newReport)
    }

    new DynamoEvent(event.toMap).records.foreach { record =>
      logger.info(f"Record: ${record.dynamodb}")
      (Report(record.dynamodb.oldItem), Report(record.dynamodb.newItem)) match {
        case (None, Some(newReport))            => insert(newReport)
        case (Some(oldReport), Some(newReport)) => modify(oldReport, newReport)
        case (Some(oldReport), None)            => remove(oldReport)
        case (None, None)                       => throw new RuntimeException("No items passed")
      }
    }
  }
}
