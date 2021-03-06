package blended.file

import java.io.ByteArrayOutputStream
import javax.jms._

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout
import blended.jms.utils.{JMSMessageHandler, JMSSupport}

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.control.NonFatal

object JMSFileDropActor {
  def props(cfg: JMSFileDropConfig) : Props = Props(new JMSFileDropActor(cfg))
}

class JMSFileDropActor(cfg: JMSFileDropConfig) extends Actor with ActorLogging {

  private[this] def dropCmd(msg: Message) : FileDropCommand = FileDropCommand(
    content = Array.empty,
    directory = Option(msg.getStringProperty(cfg.dirHeader)).getOrElse(cfg.defaultDir),
    fileName = Option(msg.getStringProperty(cfg.fileHeader)) match {
      case None => ""
      case Some(s) => s
    },
    compressed = Option(msg.getBooleanProperty(cfg.compressHeader)).getOrElse(false),
    append = Option(msg.getBooleanProperty(cfg.appendHeader)).getOrElse(false),
    timestamp = msg.getJMSTimestamp,
    properties = Option(msg.getJMSCorrelationID).map(id => "JMSCorrelationID" -> id).toMap
      ++ msg.getPropertyNames.asScala.map { pn => (pn.toString, msg.getObjectProperty(pn.toString)) }.toMap,
    dropNotification =  cfg.dropNotification
  )

  private[this] def handleError(msg : Message, error: Throwable, notify: Boolean = true) : Unit = {
    val cmd = dropCmd(msg)
    if (cfg.dropNotification && notify) context.system.eventStream.publish(FileDropResult.result(cmd, Some(error)))
    context.stop(self)
  }

  override def receive: Receive = {
    case msg : Message =>

      val requestor = sender()

      Option(msg.getStringProperty(cfg.fileHeader)) match {
        case None =>
          val eTxt = s"Message [${msg.getJMSMessageID}] is missing the filename property [${cfg.fileHeader}]"
          log.error(eTxt)
          handleError(msg, new Exception(eTxt))

        case Some(_) =>

          (msg match {
            case tMsg : TextMessage =>
              val charSet = Option(tMsg.getStringProperty(cfg.charsetHeader)).getOrElse("UTF-8")
              log.info(s"Using charset [$charSet] to file drop text message [${tMsg.getJMSMessageID}]")
              Some(tMsg.getText.getBytes(charSet))
            case bMsg: BytesMessage =>
              val buffer = new Array[Byte](1024)
              val os = new ByteArrayOutputStream()
              var count = 0
              do {
                count = bMsg.readBytes(buffer)
                if (count > 0) os.write(buffer, 0, count)
              } while(count >= 0)
              os.close()
              Some(os.toByteArray)

            case m =>
              val eTxt = s"Dropping files unsupported for msg [${m.getJMSMessageID}] of type [${m.getClass.getName}]"
              log.error(eTxt)
              handleError(m, new Exception(eTxt))
              None
          }).foreach{ content =>
            val cmd = dropCmd(msg).copy(content = content)
            cfg.system.actorOf(Props[FileDropActor]).tell(cmd, requestor)
          }
      }
  }
}

class JMSFileDropHandler(cfg: JMSFileDropConfig) extends JMSMessageHandler with JMSSupport {

  override def handleMessage(msg: Message): Option[Throwable] = {

    implicit val timeOut : Timeout= Timeout(cfg.dropTimeout.seconds)

    try {
      val fResult = (cfg.system.actorOf(JMSFileDropActor.props(cfg)) ? msg).mapTo[FileDropResult]
      val result = Await.result(fResult, timeOut.duration)
      result.error
    } catch {
      case NonFatal(t) => Some(t)
    }
  }
}
