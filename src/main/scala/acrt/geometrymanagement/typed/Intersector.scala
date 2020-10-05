package acrt.geometrymanagement.typed

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, ActorContext}
import swiftvis2.raytrace.{Geometry, Ray}
import acrt.geometrymanagement.typed
import acrt.raytracing.typed.PixelHandler

object Intersector {
  case class CastRay(k: Long, ray: Ray, rec: ActorRef[PixelHandler.IntersectResult], geomOrg: ActorRef[GeometryOrganizer.RecID])

  def apply(geom: Geometry): Behavior[CastRay] = Behaviors.receive { (context, message) =>
    val k = message.k
    context.log.info(s"Intersect Geometry $k.")
    message.geomOrg ! GeometryOrganizer.RecID(message.rec, message.k, geom intersect message.ray)
    Behaviors.same
  }
}