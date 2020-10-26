package acrt.cluster.untyped

import akka.actor.{Actor, Props, ActorRef}
import akka.routing.BalancingPool
import swiftvis2.raytrace.{Geometry, Ray}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp
import akka.actor.ActorSelection
import java.net.URL
import data.CartAndRad
import swiftvis2.raytrace._
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.MemberStatus
import akka.cluster.Member
import akka.actor.RootActorPath

class GeometryManager(cluster: Cluster, organizer: ActorRef, number: String, offset: Double) extends Actor {
  import GeometryManager._

  private var geom: Geometry = null
  private var router: ActorRef = null
  implicit val ec = context.dispatcher
  def receive = {
    case GeometryOrganizerAll.TestSerialize(geom) => {
      println(geom)
      println(geom.get.color)
    }
    case FindPath(f) => {
      geom = f(number, offset)
      router = context.actorOf(BalancingPool(Runtime.getRuntime().availableProcessors()).props(Props(new Intersector(geom))), "IntersectRouter" + scala.util.Random.nextLong())
      sender ! GeometryOrganizerAll.ReceiveDone(geom.boundingSphere)
    }
    case OrganizerRegistration => {
      organizer ! GeometryOrganizerAll.ManagerRegistration(self)
      println("registering manager with frontend")
    }
    
    case CastRay(r, k, ray, geomOrg) => {
      router ! Intersector.CastRay(k, ray, r, geomOrg)
    }
    
    case m => "GeometryManager received unhandled message: " + m
  }

}

object GeometryManager {
  case class FindPath(func: GeometryCreator) extends Serializable
  case class CastRay(recipient: ActorRef, k: Long, ray: Ray, geomOrg: ActorRef) extends Serializable
  case object OrganizerRegistration extends Serializable
}