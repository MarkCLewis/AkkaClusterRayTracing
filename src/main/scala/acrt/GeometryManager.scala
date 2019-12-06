package acrt

import akka.actor.Actor
import swiftvis2.raytrace.Geometry
import swiftvis2.raytrace.Ray
import akka.routing.BalancingPool
import akka.actor.Props
import akka.actor.ActorRef

class GeometryManager(geom: Geometry) extends Actor {
  import GeometryManager._
  //Creates a BalancingPool of Intersectors equal to core count
  val router = context.actorOf(BalancingPool(Runtime.getRuntime().availableProcessors()).props(Props(new Intersector(geom))), "IntersectRouter")

  def receive = {
    //Sends a given Ray to the router to be allocated to one of the 8 possible Intersectors
    case CastRay(r, k, ray) => {
      router ! Intersector.CastRay(k, ray, sender)
    }
    case m => "GeometryManager received unhandled message: " + m
  }
}

object GeometryManager {
  case class CastRay(recipient: ActorRef, k: Long, ray: Ray)
}