package acrt.cluster.untyped.backend

import akka.actor.{Actor, Props, ActorRef}
import akka.cluster.Cluster
import akka.routing.BalancingPool
import swiftvis2.raytrace.{Geometry, Ray}
import acrt.cluster.untyped.frontend.GeometryOrganizer
import acrt.cluster.untyped.frontend.GeometryCreator
import containers.BoxContainer

class GeometryManager(cluster: Cluster, organizer: ActorRef, number: String, xyOffset: (Double, Double)) extends Actor {
  import GeometryManager._

  private var geom: Geometry = null
  private var router: ActorRef = null
  implicit val ec = context.dispatcher

  def receive = {
    //Given the GeometryCreator, finds the manager's exGeometry and loads it, then creates a router of intersectors with it, then responds with bounds
    case FindPath(f) => {
      val rand = scala.util.Random.nextLong()
      geom = f(number, xyOffset)
      router = context.actorOf(BalancingPool(
          Runtime.getRuntime().availableProcessors()).props(Props(new Intersector(geom))), s"IntersectRouter$rand")
      println(geom.boundingBox)
      sender ! GeometryOrganizer.ReceiveDone(BoxContainer(geom.boundingBox))
    }

    //Registers with Organizer
    case OrganizerRegistration => {
      println("mgr register with organizer")
      organizer ! GeometryOrganizer.ManagerRegistration(self)
    }
    
    //Casts given ray with the intersector
    case CastRay(r, k, ray, geomOrg) => {
      //println("Casting Ray: " + k)
      router ! Intersector.CastRay(k, ray, r, geomOrg)
    }
    
    case m => "GeometryManager received unhandled message: " + m
  }

}

object GeometryManager {
  case class FindPath(func: GeometryCreator) extends CborSerializable
  case class CastRay(recipient: ActorRef, k: Long, ray: Ray, geomOrg: ActorRef) extends CborSerializable
  case object OrganizerRegistration extends CborSerializable
}
