package acrt.cluster.untyped

import akka.actor.Actor
import swiftvis2.raytrace._
import akka.actor.Props
import collection.mutable
import akka.actor.ActorSelection
import akka.actor.ActorRef
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonSubTypes

class PixelHandler(lights: List[PointLight], i: Int, j: Int, numRays: Int, organizer: ActorRef) extends Actor {
  import PixelHandler._
  private val buff = mutable.ArrayBuffer[RTColor]() 
  private var count = 0
  
  def receive = {
    case AddRay(r) => {
      organizer ! GeometryOrganizerAll.CastRay(self, scala.util.Random.nextLong(), r)
    }
    
    case IntersectResult(k: Long, intD: Option[IntersectContainer]) => {
      intD match {
        case None =>  context.parent ! ImageDrawer.SetColor(i, j, RTColor.Black)
        case Some(id) => {
          val chld = context.actorOf(Props(new LightMerger(lights, id, organizer)), s"LightMerger$i,$j,$count")
          count+=1
        }
      }
    }
    
    case SetColor(col: RTColor) => {
      buff += col
      if(buff.length >= numRays) {
        context.parent ! ImageDrawer.SetColor(i, j, buff.reduceLeft(_ + _) / numRays)
        context.stop(self)
      }
    }
    
    case m => "me pixelhandler. me receive " + m
  }
}
object PixelHandler {
  case class AddRay(r: Ray)
  case class SetColor(col: RTColor)
  case class IntersectResult(k: Long, intD: Option[IntersectContainer])
}
