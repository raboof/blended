package blended.updater.config.json

import blended.updater.config._
import prickle._
import microjson.JsValue

object PrickleProtocol {
  
  implicit val prickleConfig: PConfig[JsValue] = JsConfig(areSharedObjectsSupported = false)
  
  implicit val updateActionPickler: PicklerPair[UpdateAction] = CompositePickler[UpdateAction].
    concreteType[ActivateProfile].
    concreteType[StageProfile].
    concreteType[AddRuntimeConfig].
    concreteType[AddOverlayConfig]

  // Workaround for scala compiler error "diverging implicit expansion"
  // see https://github.com/benhutchison/prickle/issues/27
  implicit val serviceInfoPickler: Pickler[ServiceInfo] = Pickler.materializePickler[ServiceInfo]
  implicit val serviceInfoUnpickler: Unpickler[ServiceInfo] = Unpickler.materializeUnpickler[ServiceInfo]

  implicit val containerInfoPickler: Pickler[ContainerInfo] = Pickler.materializePickler[ContainerInfo]
  implicit val containerInfoUnpickler: Unpickler[ContainerInfo] = Unpickler.materializeUnpickler[ContainerInfo]
  
  implicit val overlayStatePickler: PicklerPair[OverlayState] = CompositePickler[OverlayState].
    concreteType[OverlayState.Active.type].
    concreteType[OverlayState.Valid.type].
    concreteType[OverlayState.Invalid.type].
    concreteType[OverlayState.Pending.type]

}