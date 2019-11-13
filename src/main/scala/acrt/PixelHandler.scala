package acrt

import akka.actor.Actor
import data.CartAndRad
import swiftvis2.raytrace._
import swiftvis2.raytrace.LinearViewPath._
import akka.actor.ActorRef
import akka.actor.Props

class PixelHandler(lights: List[PointLight], i: Int, j: Int) extends Actor {
  import PixelHandler._

  def receive = {
    case AddRay(r: Ray) => {
      Main.manager ! RTManager.CastRay(self, scala.util.Random.nextLong(), r)
    }
    case IntersectResult(k: Long, intD: Option[IntersectData]) => {
      intD match {
        case None =>  context.parent ! ImageDrawer.SetColor(i, j, RTColor.Black)
        case Some(id) => {
          val chld = context.actorOf(Props(new LightMerger(lights, id)), s"LightMerger$i,$j")
        }
      }
    }
    case SetColor(col: RTColor) => {
      println("REEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE")
      context.parent ! ImageDrawer.SetColor(i, j, col)
    }
    case m => "me pixelhandler. me receive " + m
  }
}
object PixelHandler {
  case class AddRay(r: Ray)
  case class SetColor(col: RTColor)
  case class IntersectResult(k: Long, intD: Option[IntersectData])
}