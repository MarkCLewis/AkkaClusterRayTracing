package acrt

import akka.actor.Actor
import swiftvis2.raytrace.Geometry
import swiftvis2.raytrace.Ray
import akka.routing.BalancingPool
import akka.actor.Props
import akka.actor.ActorRef
import swiftvis2.raytrace.IntersectData
import swiftvis2.raytrace.KDTreeGeometry

class GeometryOrganizerAll(simpleGeom: Seq[Geometry]) extends Actor {
  import GeometryOrganizerAll._
  
  val ymin = simpleGeom.minBy(_.boundingSphere.center.y).boundingSphere.center.y
  val ymax = simpleGeom.maxBy(_.boundingSphere.center.y).boundingSphere.center.y
  val numManagers = 10
  val geomSeqs = simpleGeom.groupBy(g => ((g.boundingSphere.center.y - ymin) / (ymax-ymin) * numManagers).toInt min (numManagers - 1))
  
  val geoms = geomSeqs.mapValues(gs => new KDTreeGeometry(gs))

  val geomManagers = geoms.map { case (n, g) => n -> context.actorOf(Props(new GeometryManager(g)), "GeometryManager" + n) }

  private val buffMap = collection.mutable.Map[Long, collection.mutable.ArrayBuffer[Option[IntersectData]]]() 
  def receive = {
    case CastRay(rec, k, r) => {
      buffMap += (k -> new collection.mutable.ArrayBuffer[Option[IntersectData]])
      geomManagers.foreach(_._2 ! GeometryManager.CastRay(rec, k, r, self))
    }
    case RecID(rec, k, id) => {
      val buffK = buffMap(k)
      buffK += id

      if(buffK.length < numManagers) {
        buffMap -= k
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

object GeometryOrganizerAll {
  case class CastRay(recipient: ActorRef, k: Long, r: Ray)
  case class RecID(recipient: ActorRef, k: Long, id: Option[IntersectData])
}