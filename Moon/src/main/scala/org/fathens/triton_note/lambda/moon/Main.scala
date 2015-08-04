package org.fathens.triton_note.lambda.moon

import java.util.{ Date, LinkedHashMap }

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import org.fathens.astronomy.Moon
import org.fathens.triton_note.lambda.Logger

import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper

object Main {
  def handler(event: LinkedHashMap[String, String], context: Context) = {
    val logger = new Logger(context.getLogger)
    logger.debug(f"Event ${event}")
    
    val date = new Date(event("date").toLong);
    val moon = new Moon(date);
    logger.info(f"Moon at ${date}: ${moon}")
    
    val simple = Map(
      "age" -> moon.age,
      "earth-longitude" -> moon.earth_longitude.toDouble)
    new ObjectMapper().writeValueAsString(simple.asJava)
  }
}
