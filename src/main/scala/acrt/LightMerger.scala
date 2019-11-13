package acrt

import akka.actor.Actor
import data.CartAndRad
import swiftvis2.raytrace._
import swiftvis2.raytrace.LinearViewPath._
import akka.actor.ActorRef
import akka.actor.Props
import scala.collection.mutable

class LightMerger(lights: List[PointLight], id: IntersectData) extends Actor {
  import LightMerger._
  private val buff = mutable.ArrayBuffer[RTColor]() 
  val ids = collection.mutable.Map[Long, (Ray, PointLight)]() 
  /*
      val oid = geom.intersect(outRay)
      o*/
      for(light <- lights) {
        val outRay = Ray(id.point + id.norm * 0.0001 * id.geom.boundingSphere.radius, light.point)
        val k = scala.util.Random.nextLong()
        val tup = (outRay, light)
        ids += ((k, tup))
      Main.manager ! RTManager.CastRay(self, k, outRay)
  }

  def receive = {
    case PixelHandler.IntersectResult(k: Long, oid: Option[IntersectData]) => {
      oid match {
        case None => {
          val (outRay, light) = ids(k)
          val intensity = (outRay.dir.normalize dot id.norm).toFloat
          if (intensity < 0) buff += (RTColor.Black) else buff += (light.col * intensity);
        }
        case Some(nid) => {
          if (nid.time < 0 || nid.time > 1) {
            val (outRay, light) = ids(k)
            val intensity = (outRay.dir.normalize dot id.norm).toFloat
            if (intensity < 0) buff += (RTColor.Black) else buff += (light.col * intensity);
          } else {
            buff += (RTColor.Black)
          }
        }
      }
      //println(buff.length >= lights.length)
      if(buff.length >= lights.length) {
        context.parent ! PixelHandler.SetColor(buff.foldLeft(RTColor.Black)(_ + _))
      }
    }
    case m => "me lightmerger. me receive " + m
  }
}
object LightMerger {
}