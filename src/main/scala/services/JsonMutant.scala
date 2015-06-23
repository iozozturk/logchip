package services

import com.rabbitmq.client.QueueingConsumer.Delivery

/**
 * Created by ismet özöztürk on 24/06/15.
 */
object JsonMutant extends Mutant{

  override def mutate(delivery: Delivery): Map = ???

}
