package acrt.cluster.untyped

import akka.actor.{Props, Actor, ActorRef}
import swiftvis2.raytrace.{Geometry, IntersectData, KDTreeGeometry, BoxBoundsBuilder, SphereBoundsBuilder}

class GeometryOrganizerSome(simpleGeom: Seq[Geometry]) extends Actor {
  import GeometryOrganizerAll._
  
  //Change this line for more/less breakup of geometry
  val numTotalManagers = 10
  
  //Gets the Bounds of the Geometry
  val ymin = simpleGeom.minBy(_.boundingSphere.center.y).boundingSphere.center.y
  val ymax = simpleGeom.maxBy(_.boundingSphere.center.y).boundingSphere.center.y
  
  //Groups the Geometry into slices and creates Managers for those pieces of Geometry
  val geomSeqs = simpleGeom.groupBy(g => ((g.boundingSphere.center.y - ymin) / (ymax-ymin) * numTotalManagers).toInt min (numTotalManagers - 1))
  val geoms = geomSeqs.map { case (n, gs) => n -> new KDTreeGeometry(gs, builder = SphereBoundsBuilder) }
  //val geomManagers = geoms.map { case (n, g) => n -> context.actorOf(Props(new GeometryManager(g)), "GeometryManager" + n) }

  //Creates a Map of Keys to Buffers for Rays, and a Map of Keys to the number of Managers
  private val buffMap = collection.mutable.Map[Long, collection.mutable.ArrayBuffer[Option[IntersectData]]]() 
  private val numManagersMap = collection.mutable.Map[Long, Int]()

  def receive = {
    //Casts a Ray to all Managers it would intersect and stores how many total it intersects
    case CastRay(rec, k, r) => {
      val intersects = geoms.filter(_._2.boundingSphere.intersectParam(r) != None)
      buffMap += (k -> new collection.mutable.ArrayBuffer[Option[IntersectData]])
      numManagersMap += (k -> intersects.size)

      if (intersects.isEmpty) rec ! PixelHandler.IntersectResult(k, None)
      else for(i <- intersects) {
          //geomManagers(i._1) ! GeometryManager.CastRay(rec, k, r, self)
      }
    }
    //Upon receiving IntersectData, adds it to the Buffer
    case RecID(rec, k, id) => {
      val buffK = buffMap(k)
      val numManagersK = numManagersMap(k)
      buffK += id

      //If the buffer has all rays from all managers sent to,
      //it sees if it hits any, then finds the first hit, or sends None
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
