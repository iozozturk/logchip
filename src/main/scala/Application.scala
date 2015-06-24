import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.{Sink, Flow, Source}
import io.scalac.amqp.{Delivery, Connection, Queue}
import services.{JsonMutant, JsonLog}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Created by ismet özöztürk on 21/06/15.
 */
object Application extends App with Logging {

  implicit val actorSystem = ActorSystem("logchip")

  import actorSystem.dispatcher

  implicit val materializer = ActorFlowMaterializer()

  val connection = Connection()

  setUpRabbit onComplete {
    case Success(_) =>
      init()
    case Failure(ex) =>
      logger.error("Failed to init RabbitMQ infrastructure.", ex)

  }

  def init(): Unit = {

    val rabbitConsumer: Source[Delivery, Unit] = Source(connection.consume(RabbitRegistry.inboundQueue.name))

    val jsonLogProcessor: Flow[Delivery, JsonLog, Unit] = Flow[Delivery].map(JsonMutant.mutate)

    val jsonLogStringifier: Flow[JsonLog, String, Unit] = Flow[JsonLog].map(_.toString)

    val printSink = Sink.foreach[String](println)

    val flow = rabbitConsumer via jsonLogProcessor via jsonLogStringifier to printSink

    flow.run()
  }

  def setUpRabbit(): Future[List[Queue.BindOk]] = {
    /* declare and bind inbound exchange and queue */
    Future.sequence {
      connection.exchangeDeclare(RabbitRegistry.inboundExchange) ::
        connection.queueDeclare(RabbitRegistry.inboundQueue) :: Nil
    } flatMap { _ =>
      Future.sequence {
        connection.queueBind(RabbitRegistry.inboundQueue.name, RabbitRegistry.inboundExchange.name, "") :: Nil
      }
    }
  }

}
