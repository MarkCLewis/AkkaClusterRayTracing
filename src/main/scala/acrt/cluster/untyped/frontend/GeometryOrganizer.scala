package acrt.cluster.untyped.frontend

import jnr.ffi.types.int8_t
import akka.actor.Actor
import acrt.cluster.untyped.backend.CborSerializable
import acrt.cluster.untyped.backend.containers.BoxContainer
import akka.actor.ActorRef
import swiftvis2.raytrace.Ray
import acrt.cluster.untyped.backend.containers.IntersectContainer

trait GeometryOrganizer extends Actor {
  val numFiles: Int
  val numBackends: Int
}

object GeometryOrganizer {
  case class ReceiveDone(bounds: BoxContainer) extends CborSerializable
  case class CastRay(recipient: ActorRef, k: Long, r: Ray) extends CborSerializable
  case class RecID(recipient: ActorRef, k: Long, id: Option[IntersectContainer]) extends CborSerializable
  case class ManagerRegistration(manager: ActorRef) extends CborSerializable
  case class BackendRegistration(backend: ActorRef) extends CborSerializable
  case object GetBounds extends CborSerializable
}
