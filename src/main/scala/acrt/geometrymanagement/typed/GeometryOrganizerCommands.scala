package acrt.geometrymanagement.typed

import akka.actor.typed.{ActorRef, Behavior}
import acrt.raytracing.typed.PixelHandler
import swiftvis2.raytrace.{Geometry, Ray, KDTreeGeometry, Vect, BoxBoundsBuilder, SphereBoundsBuilder, IntersectData}

object GeometryOrganizer {
  sealed trait Command
  case class CastRay(recipient: ActorRef[PixelHandler.IntersectResult], k: Long, r: Ray) extends Command
  case class RecID(recipient: ActorRef[PixelHandler.IntersectResult], k: Long, id: Option[IntersectData]) extends Command
}
