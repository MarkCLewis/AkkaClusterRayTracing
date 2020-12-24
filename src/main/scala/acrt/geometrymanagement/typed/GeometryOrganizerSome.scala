package acrt.geometrymanagement.typed

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, ActorContext}
import swiftvis2.raytrace.{Geometry, IntersectData, KDTreeGeometry, BoxBoundsBuilder, SphereBoundsBuilder, Ray, Vect}
import acrt.geometrymanagement.typed
import acrt.raytracing.typed.PixelHandler
 
object GeometryOrganizerSome {
  import GeometryOrganizer._

  def apply(simpleGeom: Seq[Geometry]): Behavior[Command] = Behaviors.receive { (context, message) =>
    val numTotalManagers = 10
  
    val ymin = simpleGeom.minBy(_.boundingSphere.center.y).boundingSphere.center.y
    val ymax = simpleGeom.maxBy(_.boundingSphere.center.y).boundingSphere.center.y
  
    val geomSeqs = simpleGeom.groupBy(g => ((g.boundingSphere.center.y - ymin) / (ymax-ymin) * numTotalManagers).toInt min (numTotalManagers - 1))
    val geoms = geomSeqs.map { case (n, gs) => n -> new KDTreeGeometry(gs, builder = SphereBoundsBuilder) }
    val geomManagers = geoms.map { case (n, g) => n -> context.spawn(GeometryManager(g), "GeometryManager" + n) }

    val buffMap = collection.mutable.Map[Long, collection.mutable.ArrayBuffer[Option[IntersectData]]]() 
    val numManagersMap = collection.mutable.Map[Long, Int]()
    
    message match {
      case CastRay(rec, k, r) => {
        val intersects = geoms.filter(_._2.boundingSphere.intersectParam(r) != None)
        buffMap += (k -> new collection.mutable.ArrayBuffer[Option[IntersectData]])
        numManagersMap += (k -> intersects.size)

        if (intersects.isEmpty) rec ! PixelHandler.IntersectResult(k, None)
        else for(i <- intersects) {
            geomManagers(i._1) ! GeometryManager.CastRay(rec, k, r, context.self)
        }
          context.log.info(s"Cast ray $k to GeometryManagers.")
      }

      case RecID(rec, k, id) => {
        val buffK = buffMap(k)
        val numManagersK = numManagersMap(k)
        buffK += id

        if(buffK.length < numManagersK) {
          buffMap += (k -> buffK)
        } else {
          val editedBuff = buffK.filter(_ != None)

          if(editedBuff.isEmpty){
            rec ! PixelHandler.IntersectResult(k, None)
          } else {
            var lowest: IntersectData = editedBuff.head match {
              case Some(intD) => intD
              case None => null
            }

            for(i <- editedBuff) {
              i match {
                case Some(intD) => {
                  if(intD.time < lowest.time) {
                    lowest = intD
                  }
                }
                case None => println("how did we get here?")
              }
            }

            rec ! PixelHandler.IntersectResult(k, Some(lowest))
          }
        }
      }
    }
    Behaviors.same
  }
}