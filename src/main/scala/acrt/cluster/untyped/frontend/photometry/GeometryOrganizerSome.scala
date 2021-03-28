package acrt.cluster.untyped.frontend.photometry

import akka.actor.{Props, Actor, ActorRef}
import swiftvis2.raytrace.{Sphere, Ray, Vect}
import acrt.cluster.untyped.backend.{IntersectContainer, GeometryManager, Backend}
import acrt.cluster.untyped.frontend.PhotometryCreator
import acrt.cluster.untyped.frontend.raytracing.PixelHandler
import acrt.cluster.untyped.frontend.raytracing.Frontend
import acrt.cluster.untyped.frontend.raytracing.GeometryOrganizerAll._
import swiftvis2.raytrace.Box
import swiftvis2.raytrace.BoundingBox
import swiftvis2.raytrace.Point

class GeometryOrganizerSome(numFiles: Int, numBackends: Int) extends Actor {
  import GeometryOrganizer._
  
  private val managers = collection.mutable.Map.empty[ActorRef, BoundingBox]
  private var backendsRegistered = 0
  private var backends = collection.mutable.Buffer.empty[ActorRef]
  
  val finderFunc = new PhotometryCreator

  private val intersectsMap = collection.mutable.Map[Long, (Ray, Array[(ActorRef, (Double, Vect, Double, Vect))])]()
  private val buffMap = collection.mutable.Map[Long, collection.mutable.ArrayBuffer[Option[IntersectContainer]]]() 
  private val numManagersMap = collection.mutable.Map[Long, Int]()

  private var num = 1

  //List of numbers to pull files from
  val numberList: List[String] = List("5000", "5001", "5002", "5003", "5004", "5005", 
     "5006", "5007", "5008", "5009", "5010", "5011", "5012", "5013", "5014", "5015", 
     "5016", "5017", "5018", "5019", "5020", "5021", "5022", "5023", "5024", "5025", 
     "5026", "5027", "5028", "5029", "6000", "6001", "6002", "6003", "6004", "6005", 
     "6006", "6007", "6008", "6009", "6010", "6011", "6012", "6013", "6014", "6015", 
     "6016", "6017", "6018", "6019", "6020", "6021", "6022", "6023", "6024", "6025",
     "6026", "6027", "6028", "6029")

  def receive = {
    
    case GetBounds => {
      val boundsList = managers.map(_._2)
      val totalBounds = boundsList.reduceLeft((bounds, b) => BoundingBox.mutualBox(b, bounds))
      println(totalBounds)
      sender ! ImageDrawer.Bounds(totalBounds.min.x, totalBounds.max.x, totalBounds.min.y, totalBounds.max.y)
      println(boundsList.mkString("\n"))  
    }

    //Receives back that the manager has finished loading data; when all have, starts drawing
    case ReceiveDone(bounds) => {
      println("Manager has loaded geometry")
      managers += (sender -> bounds.toBoundingBox)
      backendsRegistered += 1
      println("bounds = " + bounds)
      println("boundingBox = " + bounds.toBoundingBox)

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
      println("Registering with manager")
      manager ! GeometryManager.FindPath(finderFunc)
    }

    //Casts Ray to all the managers that it intersects; sends back to pixel if none
    case CastRay(rec, k, r) => {
      //num += 1
      if(num % 100 == 0) {
        println("100 rays cast")
        num = 1
      }
      val intersects = managers.filter(_._2.intersectParam(r) != None)
      buffMap += (k -> new collection.mutable.ArrayBuffer[Option[IntersectContainer]])
      numManagersMap += (k -> intersects.size)

      if (intersects.isEmpty) { rec ! PixelHandler.IntersectResult(k, None)
        //println("casting none back") 
      }
      else for(i <- intersects) {
          i._1 ! GeometryManager.CastRay(rec, k, r, self)
      }
    }

    //Receives back IntersectContainer, adds to buffer
    case RecID(rec, k, id) => {
      println("organizer received")
      val buffK = buffMap(k)
      val numManagersK = numManagersMap(k)
      buffK += id

      if(buffK.length < numManagersK) {
        buffMap += (k -> buffK)
      } else {
        //If the buffer is full, finds the first hit and sends back, else sends black
        val editedBuff = buffK.filter(_ != None)

        //println("sending back hit")
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

  //Assigns managers in a round robin to all available backends, up to the number of files
  def roundRobinManagers = {
    var x = 0
    var offset: Int = -1 * (numFiles / 2)
    while(x < numFiles) {
      val whichBackend = x % numBackends
      val whichNum = numberList(x)
      backends(x % numBackends) ! Backend.MakeManager(numberList(x), (offset, 0))
      println(s"having backend #$whichBackend make manager #$whichNum")
      offset += 1
      x += 1
    }
  }
}
