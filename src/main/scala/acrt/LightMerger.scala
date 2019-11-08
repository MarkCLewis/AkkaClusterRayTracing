package acrt

import akka.actor.Actor
import data.CartAndRad
import swiftvis2.raytrace._
import swiftvis2.raytrace.LinearViewPath._
import akka.actor.ActorRef
import akka.actor.Props
import scala.collection.mutable

class LightMerger(lights: List[Light]) extends Actor {
  import LightMerger._
  private val buff = mutable.ArrayBuffer[Option[IntersectData]]()  

  def receive = {
    case AddRay(intD: Option[IntersectData]) => {
      
      buff += intD
      if(buff.length >= lights.length) {
        val noNones = buff.flatten.toArray
        context.parent ! PixelHandler.MergeLightSources(noNones)
      }
    }
    case m => "me lightmerger. me recieve " + m
  }
}
object LightMerger {
  case class AddRay(id: Option[IntersectData])
}