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
  def intersectGeom(r: Ray, s: ActorRef, id: Long) = {
    //TODO: Use a blockingQueue
    if(cnt > 5) new RTColor(0, 0, 0, 1)
    else {
      val oIntD = geom intersect r
      oIntD match {
        case None =>
        case Some(intD) => {
          val geomSize = intD.geom.boundingSphere.radius
          //probably going to be done in the merge light source class
          val lightColors = for (light <- lights) yield light.color(intD, geom)
        }
      }
    }
  }

}
object RTActor {
  case class CastRay(i: Int, j: Int, ray: Ray, drawer: ActorRef)
}