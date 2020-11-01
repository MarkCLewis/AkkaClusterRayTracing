package acrt.cluster.untyped

import akka.actor.{Actor, ActorRef}
import swiftvis2.raytrace.{Geometry, Ray}

class Intersector(geom: Geometry) extends Actor {
  import Intersector._

  def receive = {
    case CastRay(k, ray, rec, geomOrg) => {
      val oid = geom intersect ray
      geomOrg ! GeometryOrganizerAll.RecID(rec, k, oid.map(IntersectContainer.apply))
    }
    case m => "Intersector received unhandled message: " + m
  }
}
object Intersector {
  case class CastRay(k: Long, ray: Ray, rec: ActorRef, geomOrg: ActorRef) extends CborSerializable
}