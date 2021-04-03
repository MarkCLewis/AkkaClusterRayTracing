package acrt.geometrymanagement.untyped

import akka.actor.{Props, Actor, ActorRef}
import swiftvis2.raytrace.{Geometry, IntersectData, KDTreeGeometry, BoxBoundsBuilder, SphereBoundsBuilder}
import acrt.raytracing.untyped.PixelHandler
import acrt.photometry.untyped.ImageDrawer
import swiftvis2.raytrace.Box
import swiftvis2.raytrace.BoundingBox

class GeometryOrganizerSome(val numFiles: Int, val gc: GeometryCreator) extends GeometryOrganizer {
  import GeometryOrganizer._
  
  val numberList: List[String] = List("5000", "5001", "5002", "5003", "5004", "5005", 
     "5006", "5007", "5008", "5009", "5010", "5011", "5012", "5013", "5014", "5015", 
     "5016", "5017", "5018", "5019", "5020", "5021", "5022", "5023", "5024", "5025", 
     "5026", "5027", "5028", "5029", "6000", "6001", "6002", "6003", "6004", "6005", 
     "6006", "6007", "6008", "6009", "6010", "6011", "6012", "6013", "6014", "6015", 
     "6016", "6017", "6018", "6019", "6020", "6021", "6022", "6023", "6024", "6025",
     "6026", "6027", "6028", "6029")

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

  def giveOffsets(arr: Seq[String], offsetArray: IndexedSeq[(Double, Double)]): Map[String, (Double, Double)] = {
      arr.map(t => (t, offsetArray(arr.indexOf(t)))).toMap
  }

  val wc = new WebCreator

  val offsetsMap = giveOffsets(cartAndRadNumbers, offsets)
  private var managerBounds = List[Box]()
  private def totalBounds: Box = managerBounds.reduce(BoundingBox.mutualBox(_,_))

  val managers = for(n <- cartAndRadNumbers) yield {
    val data = gc(n, offsetsMap(n))
    managerBounds =  data.boundingBox :: managerBounds
    (context.actorOf(Props(new GeometryManager(data))) -> data)
  }

  //Creates a Map of Keys to Buffers for Rays, and a Map of Keys to the number of Managers
  private val buffMap = collection.mutable.Map[Long, collection.mutable.ArrayBuffer[Option[IntersectData]]]() 
  private val numManagersMap = collection.mutable.Map[Long, Int]()

  def receive = {
    case GetBounds(s) => {
      if(managerBounds.length == numFiles) 
        s ! ImageDrawer.Bounds(totalBounds.min.x, totalBounds.max.x, totalBounds.min.y, totalBounds.max.y)
      else {
        Thread.sleep(1000)
        self ! GetBounds(s)
      }
    }
    //Casts a Ray to all Managers it would intersect and stores how many total it intersects
    case CastRay(rec, k, r) => {
      val intersects = managers.filter(_._2.boundingBox.intersectParam(r) != None)
      buffMap += (k -> new collection.mutable.ArrayBuffer[Option[IntersectData]])
      numManagersMap += (k -> intersects.size)
      //println("casting ray")

      if (intersects.isEmpty) {
        rec ! PixelHandler.IntersectResult(k, None)
        println("no geometry")
      }
      else for(i <- intersects) {
          i._1 ! GeometryManager.CastRay(rec, k, r, self)
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
