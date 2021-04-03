package acrt.cluster.untyped.frontend.raytracing

import akka.actor.{Actor, ActorRef, Props}
import swiftvis2.raytrace.Ray
import acrt.cluster.untyped.backend.{GeometryManager, IntersectContainer, Backend, CborSerializable, SphereContainer}
import acrt.cluster.untyped.frontend.WebCreator
import acrt.cluster.untyped.backend.BoxContainer
import swiftvis2.raytrace.Geometry

class GeometryOrganizerAll(numFiles: Int, numBackends: Int) extends Actor {
  import GeometryOrganizerAll._
  
  private var managers = IndexedSeq.empty[ActorRef]
  private var backends = IndexedSeq.empty[ActorRef]
  private val buffMap = collection.mutable.Map[Long, collection.mutable.ArrayBuffer[Option[IntersectContainer]]]() 
  
  val finderFunc = new WebCreator

  //List of numbers to pull files from
  val numberList: Seq[String] = Seq("5000", "5001", "5002", "5003", "5004", "5005", 
     "5006", "5007", "5008", "5009", "5010", "5011", "5012", "5013", "5014", "5015", 
     "5016", "5017", "5018", "5019", "5020", "5021", "5022", "5023", "5024", "5025", 
     "5026", "5027", "5028", "5029", "6000", "6001", "6002", "6003", "6004", "6005", 
     "6006", "6007", "6008", "6009", "6010", "6011", "6012", "6013", "6014", "6015", 
     "6016", "6017", "6018", "6019", "6020", "6021", "6022", "6023", "6024", "6025",
     "6026", "6027", "6028", "6029")

  def giveOffsets(arr: Seq[String], offsetArray: IndexedSeq[(Double, Double)]): Map[String, (Double, Double)] = {
      arr.map(t => (t, offsetArray(arr.indexOf(t)))).toMap
  }

  val cartAndRadNumbers = {
    var nlist = numberList.take(numFiles)
    while(nlist.length< numFiles) {
        nlist = (nlist ++ nlist).take(numFiles)
    }
    nlist
  }
  val n = math.sqrt(numFiles.toDouble / 10.0).ceil.toInt

  val offsets = for(x <- 0 until 10 * n; y <- 0 until n) yield {
    (x * 2.0e-5 - (10 * n - 1) * 1e-5, y * 2e-4 - (n - 1) * 1e-4)
  }

  val offsetsMap = giveOffsets(cartAndRadNumbers, offsets)

  //Assigns managers in a round robin to all available backends, up to the number of files
  def roundRobinManagers = {
    for(x <- cartAndRadNumbers.indices) {
      val whichBackend = x % numBackends
      val whichNum = cartAndRadNumbers(x)
      backends(whichBackend) ! Backend.MakeManager(whichNum, offsetsMap(whichNum))
      println(s"having backend #$whichBackend make manager #$whichNum")
    }
  }

  def receive = {
    //Receives back that the manager has finished loading data; when all have, starts drawing
    case ReceiveDone(bounds) => {
      managers = managers :+ sender
      if(managers.length >= numFiles) context.parent ! Frontend.Start
    }

    //Registers backend with organizer, once all are, RoundRobin's managers across all Backends
    case BackendRegistration(backend) => {
      backends = backends :+ backend
      if(backends.length >= numBackends) roundRobinManagers
    }

    //Registers manager with organizer, then sends the GeometryCreator to find data
    case ManagerRegistration(manager) => {
      manager ! GeometryManager.FindPath(finderFunc)
    }

    //Casts Ray to every manager
    case CastRay(rec, k, r) => {
      buffMap += (k -> new collection.mutable.ArrayBuffer[Option[IntersectContainer]])
      managers.foreach(_ ! GeometryManager.CastRay(rec, k, r, self))
    }

    //Receives back IntersectContainer, then adds to the buffer
    case RecID(rec, k, id) => {
      val buffK = buffMap(k)
      buffK += id

      if(buffK.length < numFiles) {
        buffMap -= k
        buffMap += (k -> buffK)
      } else {
        //If the buffer is full, finds the first hit and sends back, else sends black
        val editedBuff = buffK.filter(_ != None)

        if(editedBuff.isEmpty){
          rec ! PixelHandler.IntersectResult(k, None)
        } else {
          var lowest: IntersectContainer = editedBuff.head match {
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
}

object GeometryOrganizerAll {
  case class ReceiveDone(bounds: BoxContainer) extends CborSerializable
  case class CastRay(recipient: ActorRef, k: Long, r: Ray) extends CborSerializable
  case class RecID(recipient: ActorRef, k: Long, id: Option[IntersectContainer]) extends CborSerializable
  case class ManagerRegistration(manager: ActorRef) extends CborSerializable
  case class BackendRegistration(backend: ActorRef) extends CborSerializable
}
