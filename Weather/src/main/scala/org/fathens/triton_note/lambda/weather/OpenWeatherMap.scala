package org.fathens.triton_note.lambda.weather

import java.util.Date

import scala.collection.JavaConversions._
import scala.concurrent.Future

import org.fathens.triton_note.lambda.Logger

import com.fasterxml.jackson.databind.ObjectMapper

import dispatch._
import dispatch.Defaults._

case class Weather(nominal: String, temperature: Double, iconId: String)

case class GeoInfo(latitude: Double, longitude: Double)

class OpenWeatherMap(logger: Logger, apiKey: String) {
  type JMap = java.util.LinkedHashMap[String, Object]

  lazy val URL = "http://api.openweathermap.org/data/2.5"

  implicit class TypedMap(map: Map[String, Object]) {
    def getList(key: String) = map(key).asInstanceOf[java.util.ArrayList[JMap]].toList.map(_.toMap)
    def getMap(key: String) = map(key).asInstanceOf[JMap].toMap
    def getString(key: String) = map(key).asInstanceOf[java.lang.String]
    def getInt(key: String): Int = map(key).asInstanceOf[java.lang.Integer]
    def getDouble(key: String): Double = map(key).asInstanceOf[java.lang.Double]
  }

  /**
   * OpenWeatherMap.com API
   */
  private def get(subpath: String, geoinfo: GeoInfo)(parameters: (String, String)*)(reciever: Map[String, Object] => Option[Weather]): Future[Option[Weather]] = {
    val params = "APPID" -> apiKey ::
      "lat" -> f"${geoinfo.latitude.toDouble}%3.8f" ::
      "lon" -> f"${geoinfo.longitude.toDouble}%3.8f" ::
      parameters.toList
    val paramString = params.map { case (a, b) => f"${a}=${b}" }.mkString(", ")
    val path = f"${URL}/${subpath}"
    logger info f"GET: ${path}: ${paramString}"
    val site = url(URL) / subpath <<? params.toMap
    logger debug f"Dispath: ${site}"
    Http(site OK as.String).option.map(_.flatMap { text =>
      (try {
        Option {
          logger.debug(f"Parsing json: ${text}")
          val mapper = new ObjectMapper
          val map = mapper.readValue(text, classOf[JMap]).toMap
          logger.debug(f"Parsed json: ${map}")
          reciever(map)
        }
      } catch {
        case ex: Throwable =>
          ex.printStackTrace()
          None
      }
      ).flatten
    })
  }
  def getPast(date: Date, geoinfo: GeoInfo) = get("history/city", geoinfo)(
    "type" -> "hour",
    "start" -> f"${date.getTime / 1000}",
    "cnt" -> "1"
  ) { json =>
      logger debug f"Weather Result of (${geoinfo} at ${date}): ${json}"
      if (json.getInt("cnt") < 1) None else {
        val info = json.getList("list")(0)
        val wth = info.getList("weather")(0)
        val name = wth getString "main"
        val id = wth getString "icon"
        val temp = info.getMap("main").getDouble("temp") - 273.15
        Some(Weather(name, temp, id))
      }
    }
  def getCurrent(geoinfo: GeoInfo) = get("weather", geoinfo)() { json =>
    logger debug f"Weather Result of (${geoinfo}): ${json}"
    Option {
      val wth = (json getList "weather")(0)
      val name = wth getString "main"
      val id = wth getString "icon"
      val temp = json.getMap("main").getDouble("temp") - 273.15
      Weather(name, temp, id)
    }
  }
  def apply(date: Date, geoinfo: GeoInfo) = {
    if (new Date().getTime - date.getTime < 3 * 60 * 60 * 1000) {
      getCurrent(geoinfo)
    } else getPast(date, geoinfo)
  }
}
