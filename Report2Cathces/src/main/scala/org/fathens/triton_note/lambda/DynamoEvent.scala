package org.fathens.triton_note.lambda

import java.io.{ PipedInputStream, PipedOutputStream }
import java.util.{ ArrayList, LinkedHashMap }

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.fathens.triton_note.lambda.dynamodb.TypedValue

import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.transform.AttributeValueJsonUnmarshaller
import com.amazonaws.transform.{ JsonUnmarshallerContextImpl, MapUnmarshaller }
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.StringJsonUnmarshaller
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper

package object dynamodb {
  type DMap = LinkedHashMap[String, Object]

  implicit class TypedValue(src: Map[String, Object]) {
    def value[T](key: String): Option[T] = src.get(key).map(_.asInstanceOf[T])
  }

  val jsonFactory = new JsonFactory

  private object DynamoDB {
    lazy val client = new com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient()
    lazy val delegate = new com.amazonaws.services.dynamodbv2.document.DynamoDB(client)
  }
  def getTable(name: String) = DynamoDB.delegate.getTable(name)
}

package dynamodb {
  class DynamoEvent(src: Map[String, Object]) {
    lazy val records = src.value[ArrayList[DMap]]("Records").get.map(o => new DynamoRecord(o.toMap)).toList
  }

  class DynamoRecord(src: Map[String, Object]) {
    private def string(name: String) = src.value[String](name).get

    lazy val eventID = string("eventID")
    lazy val eventName = string("eventName")
    lazy val eventVersion = string("eventVersion")
    lazy val eventSource = string("eventSource")
    lazy val eventSourceARN = string("eventSourceARN")
    lazy val awsRegion = string("awsRegion")

    lazy val dynamodb = new DynamoInfo(src.value[DMap]("dynamodb").get.toMap)
  }

  class DynamoInfo(src: Map[String, Object]) {
    private def item(name: String) = src.value[DMap](name).map { m =>
      val pipeIn = new PipedInputStream
      val pipeOut = new PipedOutputStream(pipeIn)
      Future {
        new ObjectMapper().writeValue(pipeOut, m);
      }
      val context = new JsonUnmarshallerContextImpl(jsonFactory.createParser(pipeIn))
      val map = new MapUnmarshaller[String, AttributeValue](
        StringJsonUnmarshaller.getInstance,
        AttributeValueJsonUnmarshaller.getInstance
      ).unmarshall(context)
      Item.fromMap(InternalUtils.toSimpleMapValue(map))
    }

    lazy val keys = item("Keys").get
    lazy val newItem = item("NewImage")
    lazy val oldItem = item("OldImage")
    lazy val streamViewType = src.value[String]("StreamViewType").get
    lazy val sequenceNumber = src.value[Int]("SequenceNumber").get
    lazy val sizeBytes = src.value[Int]("SizeBytes").get
  }
}
