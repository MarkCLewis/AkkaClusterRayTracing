package acrt.cluster.untyped

import akka.actor.{Actor, ActorRef, Props}
import swiftvis2.raytrace._
import data.CartAndRad
import java.net.URL

class GeometryOrganizerAll extends Actor {
  import GeometryOrganizerAll._

  val numManagers = 1
  private var managers = IndexedSeq.empty[ActorRef]
  //Gets the Bounds of the Geometry
  private var ymin: Double = 0.0 //simpleGeom.minBy(_.boundingSphere.center.y).boundingSphere.center.y
  private var ymax: Double = 0.0 //simpleGeom.maxBy(_.boundingSphere.center.y).boundingSphere.center.y

  //Map of IDs to Buffers of IntersectDatas
  private val buffMap = collection.mutable.Map[Long, collection.mutable.ArrayBuffer[Option[IntersectData]]]() 
  
  val finderFunc: String => Geometry = { s =>
    val carURL = new URL("http://www.cs.trinity.edu/~mlewis/Rings/AMNS-Moonlets/Moonlet4/CartAndRad.6029.bin")
    val particles = CartAndRad.readStream(carURL.openStream).map(p => GeomSphere(Point(p.x, p.y, p.z), p.rad, _ => new RTColor(1, 1, 1, 1), _ => 0.0))
    val geom = new KDTreeGeometry(particles)
    geom
  }
  def receive = {
    case ReceiveDone => {
      managers = managers :+ sender
      if(managers.length >= numManagers)
        context.parent ! Frontend.Start
    }
    case ManagerRegistration(mgr)=> {
      mgr ! GeometryManager.FindPath(finderFunc)
    }
    //Casts Rays to every Geometry and adds the ray to the Map
    case CastRay(rec, k, r) => {
      buffMap += (k -> new collection.mutable.ArrayBuffer[Option[IntersectData]])
      managers.foreach(_ ! GeometryManager.CastRay(rec, k, r, self))
    }
    //Receives back IntersectDatas from the Managers 
    case RecID(rec, k, id) => {
      //Adds the ID to the Buffer based on the associated Key
      val buffK = buffMap(k)
      buffK += id

      //When the buffer is full of data from each Manager, chooses the first hit and sends it back,
      //or sends back None if no hits
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
  case object ReceiveDone extends CborSerializable
  case class CastRay(recipient: ActorRef, k: Long, r: Ray) extends CborSerializable
  case class RecID(recipient: ActorRef, k: Long, id: Option[IntersectData]) extends CborSerializable
  case class ManagerRegistration(manager: ActorRef) extends CborSerializable
}