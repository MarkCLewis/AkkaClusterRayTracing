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

class GeometryManager(cluster: Cluster, number: String) extends Actor {
  import GeometryManager._
  //Creates a BalancingPool of Intersectors equal to core count
  private var geom: Geometry = null
  private var frontend: ActorSelection = null

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)
  private var router: ActorRef = null

  private var organizer: ActorSelection = null 

  def receive = {
    case FindPath(f) => {
      geom = f(number)
      router = context.actorOf(BalancingPool(Runtime.getRuntime().availableProcessors()).props(Props(new Intersector(geom))), "IntersectRouter" + scala.util.Random.nextLong())
      sender ! GeometryOrganizerAll.ReceiveDone(geom.boundingSphere)
    }
    
    case CastRay(r, k, ray, geomOrg) => {
      router ! Intersector.CastRay(k, ray, r, geomOrg)
    }
    
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up).foreach(register)
    
    case MemberUp(m) => register(m)
    
    case m => "GeometryManager received unhandled message: " + m
  }

  def register(member: Member): Unit =
    if (member.hasRole("frontend")) {
      println(RootActorPath(member.address))
      frontend = context.actorSelection(RootActorPath(member.address) / "user" / "Frontend")
      frontend ! BackendRegistration
    }
}

object GeometryManager {
  case class FindPath(func: GeometryCreator) extends KryoSerializable
  case class CastRay(recipient: ActorRef, k: Long, ray: Ray, geomOrg: ActorRef) extends KryoSerializable
  case object BackendRegistration extends KryoSerializable
}