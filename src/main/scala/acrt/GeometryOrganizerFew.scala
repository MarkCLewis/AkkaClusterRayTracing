package acrt

import akka.actor.Actor
import swiftvis2.raytrace.Geometry
import swiftvis2.raytrace.Ray
import akka.routing.BalancingPool
import akka.actor.Props
import akka.actor.ActorRef
import swiftvis2.raytrace.IntersectData
import swiftvis2.raytrace.KDTreeGeometry
import swiftvis2.raytrace.Vect
import swiftvis2.raytrace.BoxBoundsBuilder
import swiftvis2.raytrace.SphereBoundsBuilder

class GeometryOrganizerFew(simpleGeom: Seq[Geometry]) extends Actor {
  import GeometryOrganizerAll._
  
  val ymin = simpleGeom.minBy(_.boundingSphere.center.y).boundingSphere.center.y
  val ymax = simpleGeom.maxBy(_.boundingSphere.center.y).boundingSphere.center.y
  val numManagers = 10
  val geomSeqs = simpleGeom.groupBy(g => ((g.boundingSphere.center.y - ymin) / (ymax-ymin) * numManagers).toInt min (numManagers - 1))
  
  val geoms = geomSeqs.map { case (n, gs) => n -> new KDTreeGeometry(gs, builder = BoxBoundsBuilder) }

  val geomManagers = geoms.map { case (n, g) => n -> context.actorOf(Props(new GeometryManager(g)), "GeometryManager" + n) }
  private val intersectsMap = collection.mutable.Map[Long, (Ray, Array[(Int, (Double, Vect, Double, Vect))])]()
  
  def receive = {
    case CastRay(rec, k, r) => {
      val intersects = geoms.map(g => g._1 -> g._2.boundingBox.intersectParam(r)).filter(g => g._2.map(_._3 > 0).getOrElse(false)).toArray.sortBy(_._2.get._3)
      //foreach/map/filter/flatmap on a none does nothing, some does func  
      if(intersects.nonEmpty) {
        geomManagers(intersects(0)._1) ! GeometryManager.CastRay(rec, k, r, self)
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
            geomManagers(intersects(0)._1) ! GeometryManager.CastRay(rec, k, r, self)
            
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