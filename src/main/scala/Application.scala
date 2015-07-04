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

  // set up the context
//  val g = FlowGraph.closed() { implicit builder =>
//    import FlowGraph.Implicits._
//
//    // create the Source and Sink
//    val in = Source(1 to 10)
//    val out = Sink.ignore
//
//    // create the fan out and fan in stages
//    val bcast = builder.add(Broadcast[Int](2))
//    val merge = builder.add(Merge[Int](2))
//
//    // create a set of transformations
//    val f1, f2, f3, f4 = Flow[Int].map(_ + 10)
//
//    // define the graph/stream processing blueprint
//    in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out
//    bcast ~> f4 ~> merge
//  }

  def setUpRabbit(): Future[List[Queue.BindOk]] = {
    /* declare and bind inbound exchange and queue */
    Future.sequence {
      connection.exchangeDeclare(RabbitRegistry.inboundExchange) ::
        connection.queueDeclare(RabbitRegistry.inboundQueue) :: Nil
    } flatMap { _ =>
      Future.sequence {
        connection.queueBind(RabbitRegistry.inboundQueue.name, RabbitRegistry.inboundExchange.name, "logchip.inbound.route") :: Nil
      }
    }
  }

}
