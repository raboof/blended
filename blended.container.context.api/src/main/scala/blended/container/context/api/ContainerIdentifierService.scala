package blended.container.context.api

import java.util.UUID

import scala.util.Try

class PropertyResolverException(msg: String) extends Exception(msg)

/**
 * Each container within the infrastructure has a unique ID. Once the unique ID is assigned to
 * a container, it doesn't change and also survives container restarts.
 * A set of user defined properties can be associated with the container id. This can be used
 * within the registration process at the data center and also to provide a simple mechanism for
 * container meta data.
 */
trait ContainerIdentifierService {
  lazy val uuid: String = UUID.randomUUID().toString()
  val properties: Map[String, String]
  val containerContext: ContainerContext

  /**
   * Try to resolved the properties inside a given String and return a string with the replaced properties values.
   */
  def resolvePropertyString(value: String): Try[String] =
    Try(ContainerPropertyResolver.resolve(this, value))
}

object ContainerIdentifierService {
  val containerId = "containerId"
}
