package org.fathens.triton_note.lambda

import java.util.{ LinkedHashMap, ArrayList }
import scala.collection.JavaConversions._

class DynamoEvent(src: LinkedHashMap[String, Object]) {
  lazy val records = src("Records").asInstanceOf[ArrayList[LinkedHashMap[String, Object]]].map(o => new DynamoRecord(o)).toList
}

class DynamoRecord(src: LinkedHashMap[String, Object]) {
  lazy val eventID = src("eventID")
  lazy val eventName = src("eventName")
  lazy val eventVersion = src("eventVersion")
  lazy val eventSource = src("eventSource")
  lazy val eventSourceARN = src("eventSourceARN")
  lazy val awsRegion = src("awsRegion")

  lazy val dynamodb = new DynamoInfo(src("dynamodb").asInstanceOf[LinkedHashMap[String, Object]])
}

class DynamoInfo(src: LinkedHashMap[String, Object]) {
  lazy val keys = src("Keys")
  lazy val newImage = Option(src("NewImage"))
  lazy val oldImage = Option(src("OldImage"))
  lazy val streamViewType = src("StreamViewType")
  lazy val sequenceNumber = src("SequenceNumber")
  lazy val sizeBytes = src("SizeBytes")
}
