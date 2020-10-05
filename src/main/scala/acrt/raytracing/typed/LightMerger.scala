package acrt.raytracing.typed

import akka.actor.Actor
import scala.collection.mutable
import swiftvis2.raytrace.{PointLight, IntersectData, RTColor, Ray}
import acrt.geometrymanagement.typed._
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, ActorContext}
import acrt.geometrymanagement.typed.{GeometryOrganizerAll, GeometryOrganizerFew, GeometryOrganizerSome}

class LightMerger(lights: List[PointLight], id: IntersectData) extends Actor {
  //Creates a buffer to contain all the RTColors needed to be merged

  def receive = {
    case m => "me lightmerger. me receive " + m
  }
}

object LightMerger {
  import PixelHandler._
  private val buff = mutable.ArrayBuffer[RTColor]() 
  val ids = collection.mutable.Map[Long, (Ray, PointLight)]() 
  
  def apply(lights: List[PointLight], id: IntersectData, parent: ActorRef[PixelHandler.PixelWork]): Behavior[PixelWork] = Behaviors.receive { (context, message) =>
    message match {
      case StartLightMerger(geomOrg: ActorRef[GeometryOrganizer.CastRay]) => {
        for(light <- lights) {
          val outRay = Ray(id.point + id.norm * 0.0001 * id.geom.boundingSphere.radius, light.point)
          val k = scala.util.Random.nextLong()
          val tup = (outRay, light)
          ids += (k -> tup)
          geomOrg ! GeometryOrganizer.CastRay(context.self, k, outRay)
        }
      }
      case IntersectResult(k: Long, oid: Option[IntersectData]) => {
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
          parent ! PixelHandler.SetColor(buff.foldLeft(RTColor.Black)(_ + _))
          context.stop(context.self)
        }
      }
      case _ =>
        context.log.warn("SOMETHING WENT WRONG WITH LIGHTMERGER")
    }
    
    Behaviors.same
  }
}