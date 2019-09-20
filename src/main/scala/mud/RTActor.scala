package mud

import akka.actor.Actor
import data.CartAndRad
import swiftvis2.raytrace._
import swiftvis2.raytrace.LinearViewPath._

class RTActor extends Actor {
  import RTActor._
  def receive = {
    case CastRay(subst) => {
      //TODO: trace subset
      //TODO: redefine render functions using actors
      //RayTrace.render(???)
      //RayTrace.castRay(ray: Ray, geom: Geometry, lights: List[Light], cnt: Int)
    }
    case _ =>
  }
}
object RTActor {
  case class CastRay(ray: Ray)
}