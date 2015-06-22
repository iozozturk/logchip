import io.scalac.amqp.{Direct, Exchange, Queue}


object RabbitRegistry {

  val inboundExchange = Exchange("logchip.inbound.exchange", Direct, true)
  val inboundQueue = Queue("logchip.inbound.queue")

}
