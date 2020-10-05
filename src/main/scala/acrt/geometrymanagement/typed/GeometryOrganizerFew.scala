package acrt.geometrymanagement.typed

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, ActorContext}
import swiftvis2.raytrace.{Geometry, Ray, KDTreeGeometry, Vect, BoxBoundsBuilder, SphereBoundsBuilder, IntersectData}
import acrt.geometrymanagement.typed
import GeometryOrganizerAll._
import acrt.raytracing.typed.PixelHandler

object GeometryOrganizerFew {
  import GeometryOrganizer._

  def apply(simpleGeom: Seq[Geometry]): Behavior[Command] = Behaviors.receive { (context, message) =>
    val numManagers = 10
  
    val ymin = simpleGeom.minBy(_.boundingSphere.center.y).boundingSphere.center.y
    val ymax = simpleGeom.maxBy(_.boundingSphere.center.y).boundingSphere.center.y
  
    val geomSeqs = simpleGeom.groupBy(g => ((g.boundingSphere.center.y - ymin) / (ymax-ymin) * numManagers).toInt min (numManagers - 1))
    val geoms = geomSeqs.map { case (n, gs) => n -> new KDTreeGeometry(gs, builder = SphereBoundsBuilder) }
    val geomManagers = geoms.map { case (n, g) => n -> context.spawn(GeometryManager(g), "GeometryManager" + n) }
  
    val intersectsMap = collection.mutable.Map[Long, (Ray, Array[(Int, (Double, Vect, Double, Vect))])]()
    
    message match {
      case CastRay(rec, k, r) => {
        val intersects = geoms.map(g => g._1 -> g._2.boundingSphere.intersectParam(r)).filter(g => g._2.map(_._3 > 0).getOrElse(false)).toArray.sortBy(_._2.get._3)
      
        if(intersects.nonEmpty) {
          geomManagers(intersects(0)._1) ! GeometryManager.CastRay(rec, k, r, context.self)
          if(intersects.length > 1) intersectsMap += (k -> (r -> intersects.tail.map(i => i._1 -> i._2.get)))
        } else {
          rec ! PixelHandler.IntersectResult(k, None)
        }
        context.log.info(s"Cast ray $k to GeometryManagers.")
      }

      case RecID(rec, k, id) => {
        id match {
          case Some(intD) => {
            rec ! PixelHandler.IntersectResult(k, id)
          } 
          case None => {
            if(intersectsMap.contains(k)) {
              val (r, intersects) = intersectsMap(k)
              geomManagers(intersects(0)._1) ! GeometryManager.CastRay(rec, k, r, context.self)
            
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
    }
    Behaviors.same
  }
}