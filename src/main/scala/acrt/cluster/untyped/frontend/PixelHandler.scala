package acrt.cluster.untyped.frontend

import scala.collection.mutable
import akka.actor.{Actor, Props, ActorRef}
import swiftvis2.raytrace.{PointLight, RTColor, Ray}
import acrt.cluster.untyped.backend.{IntersectContainer, CborSerializable}

class PixelHandler(lights: List[PointLight], i: Int, j: Int, numRays: Int, organizer: ActorRef) extends Actor {
  import PixelHandler._
  private val buff = mutable.ArrayBuffer[RTColor]() 
  private var count = 0
  
  def receive = {
    //Sends a ray to the organizer to be cast
    case AddRay(r) => {
      organizer ! GeometryOrganizerAll.CastRay(self, scala.util.Random.nextLong(), r)
    }
    
    //Receives back IntersectContainer and either sets color or creates a LightMerger to determine the color
    case IntersectResult(k: Long, intD: Option[IntersectContainer]) => {
      intD match {
        case None =>  context.parent ! ImageDrawer.SetColor(i, j, RTColor.Black)
        case Some(id) => {
          val chld = context.actorOf(Props(new LightMerger(lights, id, organizer)), s"LightMerger$i,$j,$count")
          count+=1
        }
      }
    }
    
    //sends the color up to the ImageDrawer
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
  case class AddRay(r: Ray) extends CborSerializable
  case class SetColor(col: RTColor) extends CborSerializable
  case class IntersectResult(k: Long, intD: Option[IntersectContainer]) extends CborSerializable
}
