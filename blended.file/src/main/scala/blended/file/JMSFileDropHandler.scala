package blended.file

import java.io.ByteArrayOutputStream
import javax.jms._

import akka.actor.{Actor, ActorLogging, Props}
import blended.jms.utils.{JMSMessageHandler, JMSSupport}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object JMSFileDropActor {
  def props(cfg: JMSFileDropConfig, errorHandler: JMSFileDropErrorHandler) : Props = Props(new JMSFileDropActor(cfg, errorHandler))
}

class JMSFileDropActor(cfg: JMSFileDropConfig, errorHandler: JMSFileDropErrorHandler) extends Actor with ActorLogging {

  private[this] def dropCmd(msg: Message) : FileDropCommand = FileDropCommand(
    content = Array.empty,
    directory = Option(msg.getStringProperty(cfg.dirHeader)).getOrElse(cfg.defaultDir),
    fileName = Option(msg.getStringProperty(cfg.fileHeader)) match {
      case None => ""
      case Some(s) => s
    },
    compressed = Option(msg.getBooleanProperty(cfg.compressHeader)).getOrElse(false),
    append = Option(msg.getBooleanProperty(cfg.appendHeader)).getOrElse(false),
    timestamp = msg.getJMSTimestamp(),
    properties = msg.getPropertyNames().asScala.map { pn => (pn.toString(), msg.getObjectProperty(pn.toString())) }.toMap
  )

  private[this] def handleError(msg : Message) : Unit = {
    errorHandler.handleError(msg, cfg)
    val cmd = dropCmd(msg)
    context.system.eventStream.publish(FileDropResult(cmd, false))
    context.stop(self)
  }

  override def receive: Receive = {
    case msg : Message =>
      Option(msg.getStringProperty(cfg.fileHeader)) match {
        case None =>
          log.error(s"Message [${msg.getJMSMessageID()}] is missing the filename property [${cfg.fileHeader}]")
          handleError(msg)

        case Some(fileName) =>

          (msg match {
            case tMsg : TextMessage =>
              val charSet = Option(tMsg.getStringProperty(cfg.charsetHeader)).getOrElse("UTF-8")
              log.info(s"Using charset [$charSet] to file drop text message [${tMsg.getJMSMessageID()}]")
              Some(tMsg.getText().getBytes(charSet))
            case bMsg: BytesMessage =>
              val buffer = new Array[Byte](1024)
              val os = new ByteArrayOutputStream()
              var count = 0
              do {
                count = bMsg.readBytes(buffer)
                if (count > 0) os.write(buffer, 0, count)
              } while(count >= 0)
              os.close()
              Some(os.toByteArray())

            case msg =>
              log.error(s"Dropping files unsupported for msg [${msg.getJMSMessageID()}] of type [${msg.getClass().getName()}]")
              handleError(msg)
              None
          }).foreach{ content =>
            val cmd = dropCmd(msg).copy(content = content)
            cfg.system.actorOf(Props[FileDropActor]).tell(cmd, self)
            context.become(executing(msg, cmd))
          }
      }
  }

  def executing(msg: Message, cmd: FileDropCommand) : Receive = {
    case result : FileDropResult => result.success match {
      case true =>
        context.stop(self)
      case false =>
        log.error(s"Error dropping msg [${msg.getJMSMessageID()}] to file.")
        handleError(msg)
    }
  }
}

class JMSFileDropHandler(cfg: JMSFileDropConfig, errorHandler: JMSFileDropErrorHandler) extends JMSMessageHandler with JMSSupport {

  private[this] val log = LoggerFactory.getLogger(classOf[JMSFileDropHandler])

  override def handleMessage(msg: Message): Option[Throwable] = {
    cfg.system.actorOf(JMSFileDropActor.props(cfg, errorHandler)) ! msg
    None
  }
}