package acrt.cluster.untyped.frontend.raytracing

import akka.actor.{Actor, ActorRef}
import swiftvis2.raytrace.{Ray, Vect, Sphere}
import acrt.cluster.untyped.backend._
import acrt.cluster.untyped.frontend.WebCreator
import swiftvis2.raytrace.Box

class GeometryOrganizerFew(numFiles: Int, numBackends: Int) extends Actor {
  import GeometryOrganizerAll._

  private val managers = collection.mutable.Map.empty[ActorRef, Box]
  private var backends = collection.mutable.Buffer.empty[ActorRef]
  private var backendsRegistered = 0
  
  val finderFunc = new WebCreator

  private val intersectsMap = collection.mutable.Map[Long, (Ray, Array[(ActorRef, (Double, Vect, Double, Vect))])]()
  
  //List of numbers to pull files from
  val numberList: List[String] = List("5000", "5001", "5002", "5003", "5004", "5005", 
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
      managers += (sender -> bounds)
      backendsRegistered += 1
      if(backendsRegistered >= numFiles)
        context.parent ! Frontend.Start
    }

    //Registers backend with organizer; once all are, RoundRobin's managers across all Backends
    case BackendRegistration(backend) => {
      backends = backends :+ backend
      if(backends.length >= numBackends) roundRobinManagers
    }

    //Registers manager with organizer, then sends the GeometryCreator to find data
    case ManagerRegistration(manager) => {
      manager ! GeometryManager.FindPath(finderFunc)
    }

    //Casts Ray to the first manager that it intersects; sends back to pixel if none
    case CastRay(rec, k, r) => {
      val intersects = managers.map(g => g._1 -> g._2.intersectParam(r)).filter(g => g._2.map(_._3 > 0).getOrElse(false)).toArray.sortBy(_._2.get._3)
      
      if(intersects.nonEmpty) {
        intersects(0)._1 ! GeometryManager.CastRay(rec, k, r, self)
        if(intersects.length > 1) intersectsMap += (k -> (r -> intersects.tail.map(i => i._1 -> i._2.get)))
      } else {
        rec ! PixelHandler.IntersectResult(k, None)
      }
    }

    //Receives back IntersectContainer
    case RecID(rec, k, id) => {
      id match {
        //If hits, sends to pixel
        case Some(intD) => {
          val pid = IntersectContainer(intD.time, intD.point, intD.norm, intD.color, intD.reflect, intD.geom)
          rec ! PixelHandler.IntersectResult(k, Some(pid))
        } 
        //If misses, sends to the next intersecting manager, or sends back to pixel if no more
        case None => {
          if(intersectsMap.contains(k)) {
            val (r, intersects) = intersectsMap(k)
            intersects(0)._1 ! GeometryManager.CastRay(rec, k, r, self)
            
            if(intersects.length > 1) {
              intersectsMap += (k -> (r, intersects.tail))
            } else 
              intersectsMap -= k
          } else {
            rec ! PixelHandler.IntersectResult(k, None)
          }
        }
      }
    }
    case m => "GeometryManager received unhandled message: " + m
  }
}
