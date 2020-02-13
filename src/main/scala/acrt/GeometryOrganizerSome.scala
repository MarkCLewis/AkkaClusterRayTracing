package acrt

import akka.actor.Actor
import swiftvis2.raytrace.Geometry
import swiftvis2.raytrace.Ray
import akka.routing.BalancingPool
import akka.actor.Props
import akka.actor.ActorRef
import swiftvis2.raytrace.IntersectData
import swiftvis2.raytrace.KDTreeGeometry

class GeometryOrganizerSome(simpleGeom: Seq[Geometry]) extends Actor {
  import GeometryOrganizerAll._
  
  val ymin = simpleGeom.minBy(_.boundingSphere.center.y).boundingSphere.center.y
  val ymax = simpleGeom.maxBy(_.boundingSphere.center.y).boundingSphere.center.y
  val numTotalManagers = 10
  val geomSeqs = simpleGeom.groupBy(g => ((g.boundingSphere.center.y - ymin) / (ymax-ymin) * numTotalManagers).toInt min (numTotalManagers - 1))
  
  val geoms = geomSeqs.map { case (n, gs) => n -> new KDTreeGeometry(gs) }

  val geomMans = geoms.map { case (n, g) => n -> context.actorOf(Props(new GeometryManager(g)), "GeometryManager" + n) }

  private val buffMap = collection.mutable.Map[Long, collection.mutable.ArrayBuffer[Option[IntersectData]]]() 
  private val numManMap = collection.mutable.Map[Long, Int]()

  def receive = {
    case CastRay(rec, k, r) => {
      val intscts = geoms.filter(_._2.boundingSphere.intersectParam(r) != None)
      buffMap += (k -> new collection.mutable.ArrayBuffer[Option[IntersectData]])
      numManMap += (k -> intscts.size)

      for(i <- intscts) {
          geomMans(i._1) ! GeometryManager.CastRay(rec, k, r, self)
      }
    }
    case RecID(rec, k, id) => {
      val buff = buffMap(k)
      val numManagers = numManMap(k)
      buff += id

      if(buff.length < numManagers) {
        buffMap -= k
        buffMap += (k -> buff)
      } else {
        val editBuff = buff.filter(_ != None)

        if(editBuff.isEmpty){
          rec ! PixelHandler.IntersectResult(k, None)
          println("sending none to" + rec)
        } else {
          var lowest: IntersectData = editBuff.head match {
            case Some(intD) => intD
            case None => null
          }

          for(i <- editBuff) {
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
          println("sending some to" + rec)
        }
      }
    }
    case m => "GeometryManager received unhandled message: " + m
  }
}
