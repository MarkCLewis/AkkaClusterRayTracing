package acrt.geometrymanagement.untyped

import akka.actor.{Actor, Props, ActorRef}
import akka.routing.BalancingPool
import swiftvis2.raytrace.{Geometry, Ray}

class GeometryManager(geom: Geometry) extends Actor {
  import GeometryManager._
  //Creates a BalancingPool of Intersectors equal to core count
  val router = context.actorOf(BalancingPool(Runtime.getRuntime().availableProcessors()).props(Props(new Intersector(geom))), "IntersectRouter")

  def receive = {
    //Sends a given Ray to the router to be allocated to one of the 8 (or core count) possible Intersectors
    case CastRay(r, k, ray, geomOrg) => {
      router ! Intersector.CastRay(k, ray, r, geomOrg)
    }
    case m => "GeometryManager received unhandled message: " + m
  }
}

object GeometryManager {
  case class CastRay(recipient: ActorRef, k: Long, ray: Ray, geomOrg: ActorRef)
}