package acrt.geometrymanagement.untyped

import akka.actor.{Actor, Props}
import swiftvis2.raytrace.{Geometry, Ray, KDTreeGeometry, Vect, BoxBoundsBuilder, SphereBoundsBuilder}
import acrt.raytracing.untyped.PixelHandler
import acrt.photometry.untyped.ImageDrawer
import swiftvis2.raytrace.Box
import swiftvis2.raytrace.BoundingBox
import akka.actor.ActorRef

class GeometryOrganizerFew(val numFiles: Int, val gc: GeometryCreator) extends GeometryOrganizer {
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
  
  //Creates a Map of the Key to the Intersections with the Bounds
  private val intersectsMap = collection.mutable.Map[Long, (Ray, Array[(ActorRef, (Double, Vect, Double, Vect))])]()
  
  def receive = {
    case GetBounds(s) => {
      if(managerBounds.length == numFiles) 
        s ! ImageDrawer.Bounds(totalBounds.min.x, totalBounds.max.x, totalBounds.min.y, totalBounds.max.y)
      else {
        Thread.sleep(1000)
        self ! GetBounds(s)
      }
    }
    //Casts ray to the first Manager whose Bounds the ray enters 
    case CastRay(rec, k, r) => {
      val intersects = managers.map(g => g._1 -> g._2.boundingBox.intersectParam(r)).filter(g => g._2.map(_._3 > 0).getOrElse(false)).toArray.sortBy(_._2.get._3)
      
      if(intersects.nonEmpty) {
        intersects(0)._1 ! GeometryManager.CastRay(rec, k, r, self)
        if(intersects.length > 1) intersectsMap += (k -> (r -> intersects.tail.map(i => i._1 -> i._2.get)))
      } else {
        rec ! PixelHandler.IntersectResult(k, None)
      }
    }

    //Upon receiving IntersectData, either sends the Some back up, 
    //or sends to the next manager whose bounds it hits
    case RecID(rec, k, id) => {
      id match {
        case Some(intD) => {
          rec ! PixelHandler.IntersectResult(k, id)
        } 
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