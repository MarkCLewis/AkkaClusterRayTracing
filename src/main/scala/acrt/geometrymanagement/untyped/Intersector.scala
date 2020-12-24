package acrt.geometrymanagement.untyped

import akka.actor.{Actor, ActorRef}
import swiftvis2.raytrace.{Geometry, Ray}

class Intersector(geom: Geometry) extends Actor {
  import Intersector._

  def receive = {
    // Checks if given Ray intersects the geometry and returns the result to the listed recipient, along with the supplied key
    case CastRay(k, ray, rec, geomOrg) => {
      geomOrg ! GeometryOrganizerAll.RecID(rec, k, geom intersect ray)
    }
    case m => "Intersector received unhandled message: " + m
  }
}
object Intersector {
  case class CastRay(k: Long, ray: Ray, rec: ActorRef, geomOrg: ActorRef)
}