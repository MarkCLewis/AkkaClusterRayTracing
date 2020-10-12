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
      router = context.actorOf(BalancingPool(Runtime.getRuntime().availableProcessors()).props(Props(new Intersector(geom))), "IntersectRouter")
    }
    //Sends a given Ray to the router to be allocated to one of the 8 (or core count) possible Intersectors
    case CastRay(r, k, ray, geomOrg) => {
      router ! Intersector.CastRay(k, ray, r, geomOrg)
    }
    case TransformationJob(text) => frontend ! TransformationResult(text.toUpperCase)
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up).foreach(register)
    case MemberUp(m) => register(m)
    case PixelHandler.IntersectResult(k, intD) =>
      println(s"returned ray $k with intersects $intD")
    case m => "GeometryManager received unhandled message: " + m
  }

  def register(member: Member): Unit =
    if (member.hasRole("frontend")) {
      println(RootActorPath(member.address))
      frontend = context.actorSelection(RootActorPath(member.address) / "user" / "Frontend")
      frontend ! BackendRegistration
      //frontend ! Worker.TransformationJob("dankmeme")
      //frontend ! CastRay(self, 1, Ray(Point(1,1,1),Vect(1,1,1)), organizer)
      //frontend ! GeometryOrganizerAll.CastRay(self, 1, Ray(Point(1,1,1),Vect(1,1,1)))
    }
}

object GeometryManager {
  case class FindPath(func: String => Geometry) extends CborSerializable
  case class CastRay(recipient: ActorRef, k: Long, ray: Ray, geomOrg: ActorRef) extends CborSerializable
  final case class TransformationJob(text: String) extends CborSerializable
  final case class TransformationResult(text: String) extends CborSerializable
  final case class JobFailed(reason: String, job: TransformationJob) extends CborSerializable
  case object BackendRegistration extends CborSerializable
}