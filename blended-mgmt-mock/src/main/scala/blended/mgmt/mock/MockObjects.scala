package blended.mgmt.mock

import java.text.DecimalFormat
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import blended.mgmt.base.json.JsonProtocol._
import blended.mgmt.base._
import blended.updater.config.OverlayRef
import spray.json._

import scala.collection.immutable
import scala.util.Random

object MockObjects {

  private[this] lazy val countries : List[String] = List("de", "cz", "bg", "ro")
  private[this] lazy val osTypes : List[String] = List("Lnx", "Windows")
  private[this] lazy val connectIds : List[String] = List("A", "B")

  private[this] lazy val serviceCount = new AtomicInteger(0)
  private[this] lazy val containerCount = new AtomicInteger(0)

  private[this] lazy val rnd = new Random()

  private[this] def pickOne[T](l : List[T]) : T = l(rnd.nextInt(l.size))

  private[this] def containerProps(ctNum : Integer) = Map(
    "sib.country" -> pickOne(countries),
    "sib.location" -> new DecimalFormat("00000").format(ctNum),
    "sib.connectId" -> pickOne(connectIds)
  )

  private[this] def sizedProperties(namePrefix : String = "prop", numProps : Int) =
    1.to(numProps).map(i => (s"$namePrefix-$i", s"value$i")).toMap

  private[this] def serviceInfo(numProps: Int = 10) = ServiceInfo(
    name = s"service-${serviceCount.incrementAndGet()}",
    timestampMsec = System.currentTimeMillis(),
    lifetimeMsec = 5000l,
    props = sizedProperties(namePrefix = "property", numProps = rnd.nextInt(10) + 1)
  )

  private[this] def serviceSeq(numServices : Int) : immutable.Seq[ServiceInfo] =
    1.to(numServices).map(i => serviceInfo())

  private[this] lazy val validProfiles = {

    val noOverlays = OverlaySet(
      overlays = immutable.Seq.empty[OverlayRef],
      state = OverlayState.Valid,
      reason = None
    )

    val someOverlays = OverlaySet(
      overlays = immutable.Seq(
        OverlayRef(name = "java-medium", version = "1.0"),
        OverlayRef(name = "shop-A", version = "1.0")
      ),
      state = OverlayState.Active,
      reason = None
    )

    val invalid = OverlaySet(
      overlays = immutable.Seq(
        OverlayRef(name = "java-small", version = "1.0"),
        OverlayRef(name = "shop-Q", version = "1.0")
      ),
      state = OverlayState.Invalid,
      reason = None
    )

    immutable.Seq(
      Profile(name = "blended-demo", "1.0", immutable.Seq(noOverlays)),
      Profile(name = "blended-simple", "1.0", immutable.Seq(noOverlays, someOverlays, invalid)),
      Profile(name = "blended-simple", "1.1", immutable.Seq(noOverlays, someOverlays, invalid))
    )

  }

  def createContainer(numContainers: Integer) = 1.to(numContainers).map{ i =>

    val serviceSeqs = 1.to(3).map{ i =>
      serviceSeq(rnd.nextInt(5) + 1)
    }.toList

    ContainerInfo(
      containerId = UUID.randomUUID().toString(),
      properties = containerProps(containerCount.incrementAndGet()),
      serviceInfos = pickOne(serviceSeqs),
      profiles = validProfiles
    )
  }.toList

  // use this method and one of the defined environments in the mock server
  def containerList(l: List[ContainerInfo]) = l.toJson.prettyPrint

  // Define some test environments here

  // 1. empty environment
  lazy val emptyEnv = List.empty[ContainerInfo]

  // 2. a single container environment
  lazy val minimalEnv = createContainer(1)

}

