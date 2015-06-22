import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorFlowMaterializer, FlowMaterializer}
import io.scalac.amqp.{Delivery, Connection, Queue}

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

    val rabbitConsumer = Source(connection.consume(RabbitRegistry.inboundQueue.name))
    val printSink = Sink.foreach[String](println)
    val deliveryBody =  Flow[Delivery].map(_.message.body.utf8String)
    val upperFlow = Flow[String].map(_.toUpperCase)

    val flow = rabbitConsumer via deliveryBody via upperFlow to printSink

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
