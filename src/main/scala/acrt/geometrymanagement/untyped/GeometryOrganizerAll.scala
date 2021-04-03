package acrt.geometrymanagement.untyped

import akka.actor.{Actor, ActorRef, Props}
import swiftvis2.raytrace.{Geometry, Ray, KDTreeGeometry, Vect, BoxBoundsBuilder, SphereBoundsBuilder, IntersectData}
import acrt.raytracing.untyped.PixelHandler
import acrt.photometry.untyped.ImageDrawer
import java.net.URL
import data.CartAndRad
import swiftvis2.raytrace.Point
import swiftvis2.raytrace.GeomSphere
import swiftvis2.raytrace.RTColor
import scala.concurrent.ExecutionContext
import swiftvis2.raytrace.Bounds
import swiftvis2.raytrace.BoundingBox
import swiftvis2.raytrace.Box

class GeometryOrganizerAll(val numFiles: Int, val gc: GeometryCreator) extends GeometryOrganizer {
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
  val offsetsMap = giveOffsets(cartAndRadNumbers, offsets)
  private var managerBounds = List[Box]()
  private def totalBounds: Box = managerBounds.reduce(BoundingBox.mutualBox(_,_))

  val managers = for(n <- cartAndRadNumbers) yield {
    val data = gc(n, offsetsMap(n))
    managerBounds =  data.boundingBox :: managerBounds
    context.actorOf(Props(new GeometryManager(data)))
  }

  //Map of IDs to Buffers of IntersectDatas
  private val buffMap = collection.mutable.Map[Long, collection.mutable.ArrayBuffer[Option[IntersectData]]]() 
  
  private var num = 1

  def receive = {
    case GetBounds(s) => {
      if(managerBounds.length == numFiles) 
        s ! ImageDrawer.Bounds(totalBounds.min.x, totalBounds.max.x, totalBounds.min.y, totalBounds.max.y)
      else {
        Thread.sleep(1000)
        self ! GetBounds(s)
      }
    }
    //Casts Rays to every Geometry and adds the ray to the Map
    case CastRay(rec, k, r) => {
      //num += 1
      if (num % 100 == 0) {
        println("castRay")
        num = 1
      }
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

          rec ! PixelHandler.IntersectResult(k, Some(lowest))
        }
      }
    }
    case m => "GeometryManager received unhandled message: " + m
  }
}
