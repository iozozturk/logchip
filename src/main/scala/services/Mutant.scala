package services

import io.scalac.amqp.Delivery


/**
 * Created by ismet özöztürk on 24/06/15.
 */
trait Mutant {

  def mutate(delivery : Delivery) : JsonLog

}
