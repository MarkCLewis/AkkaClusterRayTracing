package mud

import akka.actor.Actor
import data.CartAndRad
import swiftvis2.raytrace._
import swiftvis2.raytrace.LinearViewPath._
import akka.actor.ActorRef

class RTActor(geom: Geometry, lights: List[Light]) extends Actor {
  import RTActor._
  def receive = {
    case CastRay(i, j, ray, drawer) => {
      // println(s"Actor ray $i $j")
      drawer ! ImageDrawer.SetColor(i, j, RayTrace.castRay(ray, geom, lights, 0))
    }
    case _ =>
  }
}
object RTActor {
  case class CastRay(i: Int, j: Int, ray: Ray, drawer: ActorRef)
}