package acrt.cluster.untyped

import akka.actor.{Props, Actor, ActorRef}
import swiftvis2.raytrace._

class GeometryOrganizerSome(simpleGeom: Seq[Geometry]) extends Actor {
  import GeometryOrganizerAll._
  
  val numManagers = 1
  private val managers = collection.mutable.Map.empty[ActorRef, Sphere]
  private var managersRegistered = 0
  
  val finderFunc = new RingSimCreator(numManagers)

  private val intersectsMap = collection.mutable.Map[Long, (Ray, Array[(ActorRef, (Double, Vect, Double, Vect))])]()

  private val buffMap = collection.mutable.Map[Long, collection.mutable.ArrayBuffer[Option[IntersectData]]]() 
  private val numManagersMap = collection.mutable.Map[Long, Int]()

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
      val intersects = managers.filter(_._2.intersectParam(r) != None)
      buffMap += (k -> new collection.mutable.ArrayBuffer[Option[IntersectData]])
      numManagersMap += (k -> intersects.size)

      if (intersects.isEmpty) rec ! PixelHandler.IntersectResult(k, None)
      else for(i <- intersects) {
          i._1 ! GeometryManager.CastRay(rec, k, r, self)
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
