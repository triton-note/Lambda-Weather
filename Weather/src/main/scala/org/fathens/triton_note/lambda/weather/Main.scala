package org.fathens.triton_note.lambda.weather

import java.util.{ Date, LinkedHashMap }

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

import org.fathens.triton_note.lambda.Logger

import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper

object Main {
  def handler(event: LinkedHashMap[String, String], context: Context) = {
    val logger = new Logger(context.getLogger)
    logger.debug(f"Event ${event}")

    val weather = new OpenWeatherMap(logger, event("apiKey"));
    val date = new Date(event("date").toLong);
    val geoinfo = GeoInfo(event("lat").toDouble, event("lng").toDouble)
    weather(date, geoinfo).map {
      _ match {
        case None =>
        case Some(w) =>
          val map = Map(
            "nominal" -> w.nominal,
            "temperature" -> w.temperature,
            "iconId" -> w.iconId
          )
          new ObjectMapper().writeValueAsString(map.asJava)
      }
    }
  }
}
