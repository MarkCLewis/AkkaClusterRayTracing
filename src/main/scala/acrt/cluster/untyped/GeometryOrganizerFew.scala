package acrt.cluster.untyped

import akka.actor.{Actor, Props}
import swiftvis2.raytrace.{Geometry, Ray, KDTreeGeometry, Vect, BoxBoundsBuilder, SphereBoundsBuilder}
import swiftvis2.raytrace.Sphere
import akka.actor.ActorRef

class GeometryOrganizerFew(numFiles: Int) extends Actor {
  import GeometryOrganizerAll._

  val numBackends = 1
  private val managers = collection.mutable.Map.empty[ActorRef, Sphere]
  private var backends = collection.mutable.Buffer.empty[ActorRef]
  private var backendsRegistered = 0
  
  val finderFunc = new WebCreator()

  private val intersectsMap = collection.mutable.Map[Long, (Ray, Array[(ActorRef, (Double, Vect, Double, Vect))])]()
  
  val numberList: List[String] = List("5000", "5001", "5002", "5003", "5004", "5005", 
     "5006", "5007", "5008", "5009", "5010", "5011", "5012", "5013", "5014", "5015", 
     "5016", "5017", "5018", "5019", "5020", "5021", "5022", "5023", "5024", "5025", 
     "5026", "5027", "5028", "5029", "6000", "6001", "6002", "6003", "6004", "6005", 
     "6006", "6007", "6008", "6009", "6010", "6011", "6012", "6013", "6014", "6015", 
     "6016", "6017", "6018", "6019", "6020", "6021", "6022", "6023", "6024", "6025",
     "6026", "6027", "6028", "6029")

  def receive = {
    case ReceiveDone(bounds) => {
      managers += (sender -> bounds)
      backendsRegistered += 1
      if(backendsRegistered >= numBackends)
        context.parent ! Frontend.Start
    }

    case BackendRegistration => {
      backends = backends :+ sender
      if(backends.length >= numBackends) roundRobinManagers
    }

    case ManagerRegistration => {
      sender ! GeometryManager.FindPath(finderFunc)
    }

    case CastRay(rec, k, r) => {
      val intersects = managers.map(g => g._1 -> g._2.intersectParam(r)).filter(g => g._2.map(_._3 > 0).getOrElse(false)).toArray.sortBy(_._2.get._3)
      
      if(intersects.nonEmpty) {
        intersects(0)._1 ! GeometryManager.CastRay(rec, k, r, self)
        if(intersects.length > 1) intersectsMap += (k -> (r -> intersects.tail.map(i => i._1 -> i._2.get)))
      } else {
        rec ! PixelHandler.IntersectResult(k, None)
      }
    }

    case RecID(rec, k, id) => {
      id match {
        case Some(intD) => {
          val pid = IntersectContainer(intD.time, intD.point, intD.norm, intD.color, intD.reflect, intD.geom)
          rec ! PixelHandler.IntersectResult(k, Some(pid))
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