package acrt

import akka.actor.Actor
import data.CartAndRad
import swiftvis2.raytrace._
import swiftvis2.raytrace.LinearViewPath._
import akka.actor.ActorRef

class RTActor(geom: Geometry, lights: List[Light]) extends Actor {
  import RTActor._
  def receive = {
    case CastRay(k, ray, rec) => {
      // println(s"Actor ray $i $j")
      rec ! PixelHandler.IntersectResult(k, geom intersect ray)
      println("Intersected Ray #" + k)
    }
    case m => "me rtactor. me receive " + m
  }
}
object RTActor {
  case class CastRay(k: Long, ray: Ray, rec: ActorRef)
}