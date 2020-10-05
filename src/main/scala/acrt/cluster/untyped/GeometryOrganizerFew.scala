package acrt.cluster.untyped

import akka.actor.{Actor, Props}
import swiftvis2.raytrace.{Geometry, Ray, KDTreeGeometry, Vect, BoxBoundsBuilder, SphereBoundsBuilder}

class GeometryOrganizerFew(simpleGeom: Seq[Geometry]) extends Actor {
  import GeometryOrganizerAll._

  //Alternate Lines for BoxBoundsBuilder - Replace all to swap
  //val geoms = geomSeqs.mapValues(gs => new KDTreeGeometry(gs, builder = BoxBoundsBuilder))
  //val intersects = geoms.map(g => g._1 -> g._2.boundingBox.intersectParam(r)).filter(g => g._2.map(_._3 > 0).getOrElse(false)).toArray.sortBy(_._2.get._3)

  //Change this line for more/less breakup of geometry
  val numManagers = 10
  
  //Gets the Bounds of the Geometry
  val ymin = simpleGeom.minBy(_.boundingSphere.center.y).boundingSphere.center.y
  val ymax = simpleGeom.maxBy(_.boundingSphere.center.y).boundingSphere.center.y
  
  //Groups the Geometry into slices and creates Managers for those pieces of Geometry
  val geomSeqs = simpleGeom.groupBy(g => ((g.boundingSphere.center.y - ymin) / (ymax-ymin) * numManagers).toInt min (numManagers - 1))
  val geoms = geomSeqs.map { case (n, gs) => n -> new KDTreeGeometry(gs, builder = SphereBoundsBuilder) }
  val geomManagers = geoms.map { case (n, g) => n -> context.actorOf(Props(new GeometryManager(g)), "GeometryManager" + n) }
  
  //Creates a Map of the Key to the Intersections with the Bounds
  private val intersectsMap = collection.mutable.Map[Long, (Ray, Array[(Int, (Double, Vect, Double, Vect))])]()
  
  def receive = {
    //Casts ray to the first Manager whose Bounds the ray enters 
    case CastRay(rec, k, r) => {
      val intersects = geoms.map(g => g._1 -> g._2.boundingSphere.intersectParam(r)).filter(g => g._2.map(_._3 > 0).getOrElse(false)).toArray.sortBy(_._2.get._3)
      
      if(intersects.nonEmpty) {
        geomManagers(intersects(0)._1) ! GeometryManager.CastRay(rec, k, r, self)
        if(intersects.length > 1) intersectsMap += (k -> (r -> intersects.tail.map(i => i._1 -> i._2.get)))
      } else {
        rec ! PixelHandler.IntersectResult(k, None)
      }
    }

    //Upon receiving IntersectData, either sends the Some back up, 
    //or sends to the next manager whose bounds it hits
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