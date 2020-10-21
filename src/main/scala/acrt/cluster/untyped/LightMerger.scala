package acrt.cluster.untyped

import akka.actor.Actor
import scala.collection.mutable
import swiftvis2.raytrace.{PointLight, IntersectData, RTColor, Ray}
import akka.actor.ActorSelection
import akka.actor.ActorRef

class LightMerger(lights: List[PointLight], id: IntersectContainer, organizer: ActorRef) extends Actor {
  private val buff = mutable.ArrayBuffer[RTColor]() 

  val ids = collection.mutable.Map[Long, (Ray, PointLight)]() 
  
  for(light <- lights) {
    val outRay = Ray(id.point + id.norm * 0.0001 * id.geom.boundingSphere.radius, light.point)
    
    val k = scala.util.Random.nextLong()
    val tup = (outRay, light)
    ids += (k -> tup)
    
    organizer ! GeometryOrganizerAll.CastRay(self, k, outRay)
  }

  def receive = {
    case PixelHandler.IntersectResult(k: Long, oid: Option[IntersectContainer]) => {
      val (outRay, light) = ids(k)
      
      oid match {
        case None => {
          val intensity = (outRay.dir.normalize dot id.norm).toFloat
          if (intensity < 0) buff += (RTColor.Black) else buff += (light.col * intensity);
        }

        case Some(nid) => {
          if (nid.time < 0 || nid.time > 1) {
            val intensity = (outRay.dir.normalize dot id.norm).toFloat
            if (intensity < 0) buff += (RTColor.Black) else buff += (light.col * intensity);
          } else {
            buff += (RTColor.Black)
          }
        }
      }
      
      if(buff.length >= lights.length) {
        context.parent ! PixelHandler.SetColor(buff.foldLeft(RTColor.Black)(_ + _))
        context.stop(self)
      }
    }
    case m => "me lightmerger. me receive " + m
  }
}
