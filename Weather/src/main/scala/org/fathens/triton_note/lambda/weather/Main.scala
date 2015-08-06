package org.fathens.triton_note.lambda.weather

import java.util.{ Date, LinkedHashMap }

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import org.fathens.triton_note.lambda.Logger

import com.amazonaws.services.lambda.runtime.Context

object Main {
  def handler(event: LinkedHashMap[String, String], context: Context) = {
    val logger = new Logger(context.getLogger)
    logger.debug(f"Event ${event}")

    val weather = new OpenWeatherMap(logger, event("apiKey"));
    val date = new Date(event("date").toLong);
    val geoinfo = GeoInfo(event("lat").toDouble, event("lng").toDouble)
    val result = Await.result(weather(date, geoinfo), 12 seconds)
    val map = result match {
      case None => Map()
      case Some(w) => Map(
        "nominal" -> w.nominal,
        "temperature" -> w.temperature,
        "iconId" -> w.iconId
      )
    }
    map.asJava
  }
}
