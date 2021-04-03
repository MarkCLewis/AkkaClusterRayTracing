package acrt.cluster.untyped.frontend.raytracing

import scala.collection.mutable
import akka.actor.{Actor, ActorRef}
import swiftvis2.raytrace.{PointLight, RTColor, Ray}
import acrt.cluster.untyped.backend.containers.IntersectContainer
import acrt.cluster.untyped.frontend.GeometryOrganizer

class LightMerger(lights: List[PointLight], id: IntersectContainer, organizer: ActorRef) extends Actor {
  private val buff = mutable.ArrayBuffer[RTColor]() 
  val ids = collection.mutable.Map[Long, (Ray, PointLight)]() 
  
  //Sends out Rays to each light
  for(light <- lights) {
    val outRay = Ray(id.point + id.norm * 0.0001 * id.geom.boundingSphere.radius, light.point)
    
    val k = scala.util.Random.nextLong()
    val tup = (outRay, light)
    ids += (k -> tup)
    
    organizer ! GeometryOrganizer.CastRay(self, k, outRay)
  }

  def receive = {
    //Receives back IntersectContainer, and once receives all, merges the colors to create the pixel color to send up
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
