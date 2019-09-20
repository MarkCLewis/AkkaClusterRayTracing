package mud

import akka.actor.Actor
import data.CartAndRad
import swiftvis2.raytrace._
import swiftvis2.raytrace.LinearViewPath._

class RTManager(geom: Geometry, lights: List[Light]) extends Actor{
  import RTManager._
  def receive = {
    //Consider possibility of CastRays
    case CastRay(ray) => {
      sender ! RayTrace.castRay(ray, geom, lights, 0)
    }
    //TODO: need to rewrite render functions in SwiftVis RayTrace to use actors for parallelization
    //TODO: further actorize, with router
  }
}

object RTManager{
  case class CastRay(ray: Ray)
}