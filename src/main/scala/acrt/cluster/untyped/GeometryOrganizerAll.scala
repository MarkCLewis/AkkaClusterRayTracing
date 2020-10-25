package acrt.cluster.untyped

import akka.actor.{Actor, ActorRef, Props}
import swiftvis2.raytrace._
import data.CartAndRad
import java.net.URL

class GeometryOrganizerAll(numFiles: Int) extends Actor {
  import GeometryOrganizerAll._

  val numBackends = 1
  
  private var managers = IndexedSeq.empty[ActorRef]
  private var backends = IndexedSeq.empty[ActorRef]
 
  private val buffMap = collection.mutable.Map[Long, collection.mutable.ArrayBuffer[Option[IntersectData]]]() 
  
  val finderFunc = new WebCreator()

  val numberList: List[String] = List("5000", "5001", "5002", "5003", "5004", "5005", 
     "5006", "5007", "5008", "5009", "5010", "5011", "5012", "5013", "5014", "5015", 
     "5016", "5017", "5018", "5019", "5020", "5021", "5022", "5023", "5024", "5025", 
     "5026", "5027", "5028", "5029", "6000", "6001", "6002", "6003", "6004", "6005", 
     "6006", "6007", "6008", "6009", "6010", "6011", "6012", "6013", "6014", "6015", 
     "6016", "6017", "6018", "6019", "6020", "6021", "6022", "6023", "6024", "6025",
     "6026", "6027", "6028", "6029")

  def receive = {
    case ReceiveDone(bounds) => {
      managers = managers :+ sender
      if(managers.length >= numFiles) context.parent ! Frontend.Start
    }

    case BackendRegistration => {
      backends = backends :+ sender
      if(backends.length >= numBackends) roundRobinManagers
    }

    case ManagerRegistration => {
      //sender ! TestSerialize(Some(IntersectContainer(0.0, Point(0.0,0.0,0.0), Vect(0.0,0.0,0.0), RTColor.Black, 0.0, new GeomSphereContainer(Point(0,0,0), 0.0, RTColor.Black, 0.0))))
      sender ! GeometryManager.FindPath(finderFunc)
    }

    case CastRay(rec, k, r) => {
      buffMap += (k -> new collection.mutable.ArrayBuffer[Option[IntersectData]])
      managers.foreach(_ ! GeometryManager.CastRay(rec, k, r, self))
    }

    case RecID(rec, k, id) => {
      val buffK = buffMap(k)
      buffK += id

      if(buffK.length < numFiles) {
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
          val pidLowest = IntersectContainer(lowest.time, lowest.point, lowest.norm, lowest.color, lowest.reflect, lowest.geom)
          rec ! PixelHandler.IntersectResult(k, Some(pidLowest))
        }
      }
    }
    case m => "GeometryManager received unhandled message: " + m
  }

  def roundRobinManagers = {
    var x = 0
    var offset: Int = -1 * (numFiles / 2)
    while(x < numFiles) {
      for(backend <- backends) {
        if(x < numFiles) backend ! Backend.MakeManager(numberList(x), offset)
        offset += 1
        x += 1
      }
    }
  }
}

object GeometryOrganizerAll {
  case class TestSerialize(g: Option[IntersectContainer]) extends Serializable
  case class ReceiveDone(bounds: Sphere) extends Serializable
  case class CastRay(recipient: ActorRef, k: Long, r: Ray) extends Serializable
  case class RecID(recipient: ActorRef, k: Long, id: Option[IntersectData]) extends Serializable
  case object ManagerRegistration extends Serializable
  case object BackendRegistration extends Serializable
}