package acrt

import akka.actor.Actor
import swiftvis2.raytrace._
import scala.collection.mutable

class LightMerger(lights: List[PointLight], id: IntersectData) extends Actor {
  //Creates a buffer to contain all the RTColors needed to be merged
  private val buff = mutable.ArrayBuffer[RTColor]() 
  //creates a Map that assigns a random key to the Ray and Light that were used for that Ray
  val ids = collection.mutable.Map[Long, (Ray, PointLight)]() 
  for(light <- lights) {
    //Creates a Ray for each Light
    val outRay = Ray(id.point + id.norm * 0.0001 * id.geom.boundingSphere.radius, light.point)
    //Assigns that ray and the light to the given id in the map
    val k = scala.util.Random.nextLong()
    val tup = (outRay, light)
    ids += (k -> tup)
    //Sends to check if the ray intersects the geometry
    Main.organizer ! GeometryOrganizerAll.CastRay(self, k, outRay)
  }

  def receive = {
    case PixelHandler.IntersectResult(k: Long, oid: Option[IntersectData]) => {
      //Upon receiving back the results of the above rays, checks whether or not this Ray intersected the geometry
      val (outRay, light) = ids(k)
      oid match {
        case None => {
          //If no intersection, determines intensity
          val intensity = (outRay.dir.normalize dot id.norm).toFloat
          if (intensity < 0) buff += (RTColor.Black) else buff += (light.col * intensity);
        }
        case Some(nid) => {
          //If intersection, determines time to hit and then either decides intensity or black
          if (nid.time < 0 || nid.time > 1) {
            val intensity = (outRay.dir.normalize dot id.norm).toFloat
            if (intensity < 0) buff += (RTColor.Black) else buff += (light.col * intensity);
          } else {
            buff += (RTColor.Black)
          }
        }
      }
      //If all rays have returned for the light, merges colors and sends up hierarchy
      if(buff.length >= lights.length) {
        context.parent ! PixelHandler.SetColor(buff.foldLeft(RTColor.Black)(_ + _))
        context.stop(self)
      }
    }
    case m => "me lightmerger. me receive " + m
  }
}
