package org.fathens.triton_note.lambda.report2catches

import scala.collection.JavaConversions._

import org.fathens.triton_note.lambda.Logger
import org.fathens.triton_note.lambda.dynamodb.{ DMap, DynamoEvent, getTable }

import com.amazonaws.services.dynamodbv2.document.{ Item, PrimaryKey }
import com.amazonaws.services.lambda.runtime.Context

object Main {
  def handler(event: DMap, context: Context) {
    val logger = new Logger(context.getLogger);
    logger.info(f"Passed records: ${event}");

    lazy val table = getTable("TritonNote-TEST.CATCHES")

    def toFishes(report: Item) = {
      logger.debug(f"Parsing report to fishes: ${report}")
      val reportId = report.getString("ID")
      val userId = report.getString("USER_ID")
      val dateAt = report.getInt("DATE_AT")
      val content = report.getMap[DMap]("CONTENT")
      content("fishes").asInstanceOf[java.util.ArrayList[DMap]].toList.zipWithIndex.map {
        case (fish, index) =>
          new Item()
            .withString("REPORT_ID", reportId)
            .withInt("FISH_INDEX", index)
            .withString("USER_ID", userId)
            .withInt("DATE_AT", dateAt)
            .withMap("CONDITION", content("condition"))
            .withMap("LOCATION", content("location"))
            .withMap("FISH", fish)
      }
    }

    def insert(report: Item) {
      toFishes(report).foreach(table.putItem)
    }
    def remove(report: Item) {
      val reportId = report.getString("REPORT_ID")
      val fishIndex = report.getInt("FISH_INDEX")
      table.deleteItem(new PrimaryKey(
        "REPORT_ID", report.getString("REPORT_ID"),
        "FISH_INDEX", report.getInt("FISH_INDEX")))
    }
    def modify(oldReport: Item, newReport: Item) {
      remove(oldReport)
      insert(newReport)
    }

    new DynamoEvent(event.toMap).records.foreach { record =>
      logger.info(f"Record: ${record.dynamodb}")
      record.eventName match {
        case "INSERT" => insert(record.dynamodb.newItem.get)
        case "MODIFY" => modify(record.dynamodb.oldItem.get, record.dynamodb.newItem.get)
        case "REMOVE" => remove(record.dynamodb.oldItem.get)
      }
    }
  }
}
