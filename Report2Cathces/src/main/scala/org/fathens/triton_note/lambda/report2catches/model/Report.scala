package org.fathens.triton_note.lambda.report2catches.model

import java.util.ArrayList

import scala.collection.JavaConversions._

import org.fathens.triton_note.lambda.dynamodb.DMap

import com.amazonaws.services.dynamodbv2.document.Item

object Report {
  def apply(src: Option[Item]) = src.map(new Report(_))
}
class Report(src: Item) {
  lazy val id = src.getString("ID")
  lazy val userId = src.getString("USER_ID")
  lazy val dateAt = src.getInt("DATE_AT")
  
  private lazy val content = src.getMap[DMap]("CONTENT")

  lazy val location = content("location")
  lazy val condition = content("condition")
  lazy val fishes = content("fishes").asInstanceOf[ArrayList[DMap]]
  
  def toFishes = fishes.zipWithIndex.map {
    case (fish, index) =>
      Catches(this, index, fish)
  }
}
