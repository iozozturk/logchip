package services

import com.rabbitmq.client.QueueingConsumer.Delivery

/**
 * Created by ismet özöztürk on 24/06/15.
 */
trait Mutant {

  def mutate(delivery : Delivery) : Map

}
