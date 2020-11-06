package acrt.cluster.untyped.backend

import akka.actor.{Actor, ActorRef}
import swiftvis2.raytrace.{Geometry, Ray}
import acrt.cluster.untyped.frontend.GeometryOrganizerAll

class Intersector(geom: Geometry) extends Actor {
  import Intersector._

  def receive = {
    //Intersects the ray, then places in IntersectContainer for serializer
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