package acrt

import akka.actor.Actor
import swiftvis2.raytrace.Geometry
import swiftvis2.raytrace.OffsetGeometry
import swiftvis2.raytrace.Ray
import akka.actor.Props
import akka.actor.ActorRef
import swiftvis2.raytrace.Vect
import swiftvis2.raytrace.IntersectData
import swiftvis2.raytrace.Point
import scala.collection.mutable
import swiftvis2.raytrace.RTColor

class RTManager(geom: Geometry) extends Actor {
  import RTManager._
  //TODO: add key to rids
  private val rids = mutable.Map[Long, (ActorRef, mutable.ArrayBuffer[Option[IntersectData]])]()

  val geoMan = context.actorOf(Props(new GeometryManager(OffsetGeometry(geom, Vect(-1, 0, 0)))))
  val geoMan2 = context.actorOf(Props(new GeometryManager(OffsetGeometry(geom, Vect(1, 0, 0)))))

  def receive = {
    //Sends a given Ray to the router to be allocated to one of the 8 possible Intersectors
    case RTManager.CastRay(r, k, ray) => {
      val key = scala.util.Random.nextLong()
      geoMan ! GeometryManager.CastRay(self, key, ray)
      geoMan2 ! GeometryManager.CastRay(self, key, ray)
      rids += ((k, (r, mutable.ArrayBuffer[Option[IntersectData]]())))
    }
    case PixelHandler.IntersectResult(k, oid) => {
      val (rec, abuff) = rids(k)
      abuff += oid
      rids -= k
      if(abuff.length < 2) {
          rids += (k -> (rec -> abuff))
      } else {
          var lowest = Double.MaxValue
          var lowestID = IntersectData(0, Point(0,0,0), Vect(0,0,0), RTColor.Black, 0, geom) 
          var changed = false
          for(oid <- abuff){
              oid match {
                  case None => 
                  case Some(id) => 
                    if(id.time < lowest) {
                        lowest = id.time
                        lowestID = id
                        changed = true
                    }
              }
          }
          if(changed) {
              rec ! PixelHandler.IntersectResult(k, Some(lowestID))
          } else {
            rec ! PixelHandler.IntersectResult(k, None)
          }
      }
    }
    case m => "GeometryManager received unhandled message: " + m
  }
}

object RTManager {
  case class CastRay(recipient: ActorRef, k: Long, ray: Ray)
}