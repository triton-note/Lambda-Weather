package org.fathens.triton_note.lambda.report2catches

import java.util.{ LinkedHashMap, ArrayList }
import scala.collection.JavaConversions._
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import org.fathens.triton_note.lambda.Logger
import org.fathens.triton_note.lambda.DynamoEvent
import org.fathens.triton_note.lambda.DynamoRecord

object Main {
  def handler(event: LinkedHashMap[String, Object], context: Context) {
    val logger = new Logger(context.getLogger);
    logger.info(f"Passed records: ${event}");

    new DynamoEvent(event).records.foreach { record =>
      logger.info(f"Record: ${record.dynamodb}")
      val image = record.dynamodb.oldImage orElse record.dynamodb.newImage
    }
  }
}
