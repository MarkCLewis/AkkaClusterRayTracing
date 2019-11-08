package acrt

import akka.actor.Actor
import data.CartAndRad
import swiftvis2.raytrace._
import swiftvis2.raytrace.LinearViewPath._
import akka.actor.ActorRef
import akka.actor.Props

class PixelHandler(lights: List[Light]) extends Actor {
  import PixelHandler._

  val chld = context.actorOf(Props(new LightMerger(lights)), "jeff")

  def receive = {
    case AddRay(id: IntersectData) => {
      Main.imageDrawer.manager ! RTManager.CastRay(self, scala.util.Random.nextLong(), Ray(id.point + id.norm * 0.0001 * id.geom.boundingSphere.radius, point))
    }
    case AddID(intD: IntersectData) => {
      chld ! 
    }
    case m => "me pixelhandler. me recieve " + m
  }
}
object PixelHandler {
  case class AddRay(intD: IntersectData)
  case class MergeLightSources(id: Array[IntersectData])
  case class AddID(intD: IntersectData)
}