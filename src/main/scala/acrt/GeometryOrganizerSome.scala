package acrt

import akka.actor.Actor
import swiftvis2.raytrace.Geometry
import swiftvis2.raytrace.Ray
import akka.routing.BalancingPool
import akka.actor.Props
import akka.actor.ActorRef
import swiftvis2.raytrace.IntersectData
import swiftvis2.raytrace.KDTreeGeometry
import swiftvis2.raytrace.OffsetGeometry
import swiftvis2.raytrace.Vect
import swiftvis2.raytrace.BoxBoundsBuilder

class GeometryOrganizerSome(simpleGeom: Seq[Geometry]) extends Actor {
  import GeometryOrganizerAll._
  
  val numManagers = 10
  val complexGeom = new KDTreeGeometry(simpleGeom, 5, BoxBoundsBuilder)
  //val complexGeom = new KDTreeGeometry(simpleGeom, 5, SphereBoundsBuilder)
  val offsets = (0 to 9).map(i => (i, i*2e-5)).toMap
  val geoms = offsets.map { case (n, os) => n -> OffsetGeometry(complexGeom, Vect(os, 0.0, 0.0)) }  

  //val geomManagers = geoms.map { case (n, g) => n -> context.actorOf(Props(new GeometryManager(g)), "GeometryManager" + n) }

  private val buffMap = collection.mutable.Map[Long, collection.mutable.ArrayBuffer[Option[IntersectData]]]() 
  private val numManagersMap = collection.mutable.Map[Long, Int]()

  def receive = {
    case CastRay(rec, k, r) => {
      val intersects = geoms.filter(_._2.boundingBox.intersectParam(r) != None)
      //val intersects = geoms.filter(_._2.boundingSphere.intersectParam(r) != None)
      buffMap += (k -> new collection.mutable.ArrayBuffer[Option[IntersectData]])
      numManagersMap += (k -> intersects.size)

      for(i <- intersects) {
      //    geomManagers(i._1) ! GeometryManager.CastRay(rec, k, r, self)
      }
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
    case m => "GeometryManager received unhandled message: " + m
  }
}
