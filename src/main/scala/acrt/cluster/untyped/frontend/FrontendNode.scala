package acrt.cluster.untyped.frontend

import akka.actor.Actor
import acrt.cluster.untyped.backend.CborSerializable

trait FrontendNode extends Actor {
  val img: rendersim.RTBufferedImage
  val numRays: Int
}

object FrontendNode {
  case object Start extends CborSerializable
  case object BackendRegistration extends CborSerializable
  case object KillCluster extends CborSerializable
}
