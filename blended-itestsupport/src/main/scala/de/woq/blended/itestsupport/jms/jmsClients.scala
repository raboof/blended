/*
 * Copyright 2014ff, WoQ - Way of Quality GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.woq.blended.itestsupport.jms

import javax.jms._
import akka.actor.{Cancellable, ActorRef, ActorLogging, Actor}
import akka.event.LoggingReceive

import de.woq.blended.itestsupport.jms.protocol._
import scala.concurrent.duration._

trait JMSSupport {

  val TOPICTAG = "topic:"
  val QUEUETAG = "queue:"

  def jmsConnection : Connection

  def withSession(f: (Session => Unit)) {

    var session : Option[Session] = None
    try {
      session = Some(jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE))
      f(session.get)
    } finally {
      session.foreach { s =>
        s.close() }
    }
  }

  def destination(session: Session, destName: String) : Destination = {
    if (destName.startsWith(TOPICTAG))
      session.createTopic(destName.substring(TOPICTAG.length))
    else if (destName.startsWith(QUEUETAG))
      session.createQueue(destName.substring(QUEUETAG.length))
    else
      session.createQueue(destName)
  }

}

object Producer {

  def apply(connection: Connection, destName: String) =
    new Producer(connection, destName)
}

class Producer(connection: Connection, destName: String) extends JMSSupport with Actor with ActorLogging {

  override def jmsConnection = connection

  override def receive = LoggingReceive {

    case produce : ProduceMessage => {
      withSession { session =>
        log.debug(s"Sending message to [${destName}]")
        val dest = destination(session, destName)
        val producer = session.createProducer(null)
        val msg = produce.msgFactory.createMessage(session)
        producer.send(
          dest,
          msg,
          produce.deliveryMode,
          produce.priority,
          produce.ttl
        )
      }
      sender ! MessageProduced
    }
  }
}

class AkkaConsumer(
  consumerFor: ActorRef,
  connection: Connection,
  destName: String,
  subscriberName: Option[String] = None
) extends MessageListener with JMSSupport {

  override def jmsConnection = connection

  var session : Option[Session] = None
  var consumer : Option[MessageConsumer] = None

  def start() {

    session = Some(connection.createSession(false, Session.AUTO_ACKNOWLEDGE))

    session.foreach { s =>
      val dest = destination(s, destName)
      consumer = Some((subscriberName.isDefined && dest.isInstanceOf[Topic]) match {
        case true => s.createDurableSubscriber(dest.asInstanceOf[Topic], subscriberName.get)
        case _ => s.createConsumer(dest)
      })
      consumer.foreach { c => c.setMessageListener(this) }
    }
  }

  def unsubscribe() {
    consumer.foreach { c => c.close() }

    for (
      s <- session;
      subName <- subscriberName
    ) {
      s.unsubscribe(subName)
    }

    stop()
  }

  def stop() {
    session.foreach { _.close() }
    consumerFor ! ConsumerStopped
  }

  override def onMessage(msg: Message) { consumerFor ! msg }
}

object Consumer {
  def apply(connection: Connection, destName: String, subscriberName: Option[String]) =
    new Consumer(connection, destName, subscriberName)
}

class Consumer(connection: Connection, destName: String, subscriberName: Option[String]) extends Actor with ActorLogging {

  implicit val eCtxt = context.dispatcher

  val idleTimeout = 5.seconds

  var msgCount : Int = 0
  var jmsConsumer : AkkaConsumer = _
  var idleTimer : Option[Cancellable] = None

  case object MsgTimeout

  override def preStart() {
    super.preStart()

    jmsConsumer = new AkkaConsumer(self, connection, destName, subscriberName)
    jmsConsumer.start()

    resetTimer()
  }

  override def receive = LoggingReceive {
    case msg : Message => {
      log.debug(s"Received message ...")
      idleTimer.foreach { _.cancel() }
      msgCount += 1
      if (msgCount % 100 == 0) {
        log.info(s"Consumer at [${msgCount}] messages.")
      }
      resetTimer()
    }
    case Unsubscribe => {
      log.info(s"Unsubscribing [${subscriberName}]")
      jmsConsumer.unsubscribe()
    }
    case ConsumerStopped => {
      log.debug("Consumer stopped")
      idleTimer.foreach { _.cancel() }
      context.system.eventStream.publish(MessageCount(self, msgCount))
    }
    case MsgTimeout => {
      log.info(s"No message received in [${idleTimeout}]. Stopping subscriber.")
      jmsConsumer.stop()
    }
    case StopConsumer => jmsConsumer.stop()
  }

  private def resetTimer() {
    idleTimer.foreach { _.cancel() }
    idleTimer = Some(context.system.scheduler.scheduleOnce(idleTimeout, self, MsgTimeout))
  }
}
