package acrt.geometrymanagement.untyped

import akka.actor.ActorRef
import swiftvis2.raytrace.Ray
import swiftvis2.raytrace.IntersectData
import akka.actor.Actor
import swiftvis2.raytrace.Bounds

trait GeometryOrganizer extends Actor {
  val numFiles: Int
  val gc: GeometryCreator
  def receive: Receive
}

object GeometryOrganizer {
  case class CastRay(recipient: ActorRef, k: Long, r: Ray)
  case class RecID(recipient: ActorRef, k: Long, id: Option[IntersectData])
  case class GetBounds(sender: ActorRef)
  case class ManagerBounds(bounds: Bounds)
}