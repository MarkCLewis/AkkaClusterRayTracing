package acrt.cluster.untyped

import akka.actor.{Actor, ActorRef, Props}
import swiftvis2.raytrace._
import data.CartAndRad
import java.net.URL

class GeometryOrganizerAll extends Actor {
  import GeometryOrganizerAll._

  val numManagers = 1
  private var managers = IndexedSeq.empty[ActorRef]
 
  private val buffMap = collection.mutable.Map[Long, collection.mutable.ArrayBuffer[Option[IntersectData]]]() 
  
  val finderFunc = new WebCreator(numManagers)

  def receive = {
    case ReceiveDone(bounds) => {
      managers = managers :+ sender
      if(managers.length >= numManagers)
        context.parent ! Frontend.Start
    }

    case ManagerRegistration(mgr)=> {
      mgr ! TestSerialize(new GeomSphere(Point(0,0,0), 0.0, (g => RTColor.Black), (g => 0.0)))
      mgr ! GeometryManager.FindPath(finderFunc)
    }

    case CastRay(rec, k, r) => {
      buffMap += (k -> new collection.mutable.ArrayBuffer[Option[IntersectData]])
      managers.foreach(_ ! GeometryManager.CastRay(rec, k, r, self))
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
  case class TestSerialize(g: GeomSphere)
  case class ReceiveDone(bounds: Sphere) 
  case class CastRay(recipient: ActorRef, k: Long, r: Ray) 
  case class RecID(recipient: ActorRef, k: Long, id: Option[IntersectData]) 
  case class ManagerRegistration(manager: ActorRef) 
}