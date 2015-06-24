package services

import io.scalac.amqp.Delivery
import play.api.libs.json.{JsResult, Json}

/**
 * Created by ismet özöztürk on 24/06/15.
 */

case class JsonLog(context:String, level:String, timestamp:Long, loggerName:String, threadName:String, message:String)

object JsonMutant extends Mutant{
  implicit val personFormat = Json.format[JsonLog]

  override def mutate(delivery: Delivery): JsonLog = {
    val jsonLog: JsResult[JsonLog] = Json.fromJson[JsonLog](Json.parse(delivery.message.body.utf8String))
    jsonLog.get
  }

}
