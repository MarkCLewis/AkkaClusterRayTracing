package acrt.cluster.untyped

import akka.actor.{Actor, Props}
import swiftvis2.raytrace.{Geometry, Ray, KDTreeGeometry, Vect, BoxBoundsBuilder, SphereBoundsBuilder}
import swiftvis2.raytrace.Sphere
import akka.actor.ActorRef

class GeometryOrganizerFew() extends Actor {
  import GeometryOrganizerAll._

  val numManagers = 7
  private val managers = collection.mutable.Map.empty[ActorRef, Sphere]
  private var managersRegistered = 0
  
  val finderFunc = new WebCreator(numManagers)

  private val intersectsMap = collection.mutable.Map[Long, (Ray, Array[(ActorRef, (Double, Vect, Double, Vect))])]()
  
  def receive = {
    case ReceiveDone(bounds) => {
      managers += (sender -> bounds)
      managersRegistered += 1
      if(managersRegistered >= numManagers)
        context.parent ! Frontend.Start
    }

    case ManagerRegistration(mgr)=> {
      mgr ! GeometryManager.FindPath(finderFunc)
    }

    case CastRay(rec, k, r) => {
      val intersects = managers.map(g => g._1 -> g._2.intersectParam(r)).filter(g => g._2.map(_._3 > 0).getOrElse(false)).toArray.sortBy(_._2.get._3)
      
      if(intersects.nonEmpty) {
        intersects(0)._1 ! GeometryManager.CastRay(rec, k, r, self)
        if(intersects.length > 1) intersectsMap += (k -> (r -> intersects.tail.map(i => i._1 -> i._2.get)))
      } else {
        rec ! PixelHandler.IntersectResult(k, None)
      }
    }

    case RecID(rec, k, id) => {
      id match {
        case Some(intD) => {
          rec ! PixelHandler.IntersectResult(k, id)
        } 
        case None => {
          if(intersectsMap.contains(k)) {
            val (r, intersects) = intersectsMap(k)
            intersects(0)._1 ! GeometryManager.CastRay(rec, k, r, self)
            
            if(intersects.length > 1) {
              intersectsMap += (k -> (r, intersects.tail))
            } else 
              intersectsMap -= k
          } else {
            rec ! PixelHandler.IntersectResult(k, None)
          }
        }
      }
    }
    case m => "GeometryManager received unhandled message: " + m
  }
}