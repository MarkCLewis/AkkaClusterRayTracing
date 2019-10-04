package mud

import akka.actor.Actor
import data.CartAndRad
import swiftvis2.raytrace._
import swiftvis2.raytrace.LinearViewPath._

class RTActor(geom: Geometry, lights: List[Light]) extends Actor {
  import RTActor._
  def receive = {
    case CastRay(ray) => {
      sender ! RayTrace.castRay(ray, geom, lights, 0)
    }
    case _ =>
  }
}
object RTActor {
  case class CastRay(ray: Ray)
}