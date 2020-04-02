package acrt

import akka.actor.Actor
import swiftvis2.raytrace.Geometry
import swiftvis2.raytrace.Ray
import akka.actor.ActorRef

class Intersector(geom: Geometry) extends Actor {
  import Intersector._

  def receive = {
    case CastRay(k, ray, rec, geomOrg) => {
      // Checks if given Ray intersects the geometry and returns the result to the listed recipient, along with the supplied key
      geomOrg ! GeometryOrganizerAll.RecID(rec, k, geom intersect ray)
      println("intersecting")
    }
    case m => "Intersector received unhandled message: " + m
  }
}
object Intersector {
  case class CastRay(k: Long, ray: Ray, rec: ActorRef, geomOrg: ActorRef)
}