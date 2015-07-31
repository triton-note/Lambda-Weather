package org.fathens.triton_note.lambda.report2catches.model

import scala.collection.JavaConversions._

import org.fathens.triton_note.lambda.dynamodb.DMap

import com.amazonaws.services.dynamodbv2.document.{ Item, PrimaryKey }

object Catches {
  def makePrimaryKeys(report: Report) = report.fishes.zipWithIndex.map {
    case (_, index) =>
      new PrimaryKey("REPORT_ID", report.id, "FISH_INDEX", index)
  }
}
case class Catches(report: Report, index: Int, fish: DMap) {
  def asItem = new Item()
    .withString("REPORT_ID", report.id)
    .withInt("FISH_INDEX", index)
    .withString("USER_ID", report.userId)
    .withInt("DATE_AT", report.dateAt)
    .withMap("CONDITION", report.condition)
    .withMap("LOCATION", report.location)
    .withMap("FISH", fish)
}
