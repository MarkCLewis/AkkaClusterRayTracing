package acrt.cluster.untyped

import akka.actor._
import akka.cluster._
import akka.cluster.ClusterEvent._
import swiftvis2.raytrace.Ray
import _root_.swiftvis2.raytrace.Point
import swiftvis2.raytrace.Vect
import java.net.URL
import data.CartAndRad
import swiftvis2.raytrace.GeomSphere
import swiftvis2.raytrace.RTColor
//#worker
class Worker(cluster: Cluster) extends Actor {
  import Worker._
  var frontend: ActorSelection = null
    // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  val carURL = new URL("http://www.cs.trinity.edu/~mlewis/Rings/AMNS-Moonlets/Moonlet4/CartAndRad.6029.bin")
  val particles = CartAndRad.readStream(carURL.openStream).map(p => GeomSphere(Point(p.x, p.y, p.z), p.rad, _ => new RTColor(1, 1, 1, 1), _ => 0.0))
  val organizer = context.actorOf(Props(new GeometryOrganizerAll(particles)), "GeomOrganizer")

  def receive = {
    case TransformationJob(text) => frontend ! TransformationResult(text.toUpperCase)
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up).foreach(register)
    case MemberUp(m) => register(m)
    case PixelHandler.IntersectResult(k, intD) =>
      println(s"returned ray $k with intersects $intD")
    case m => println("garbled text " + m)
  }

  def register(member: Member): Unit =
    if (member.hasRole("frontend")) {
      println(RootActorPath(member.address))
      frontend = context.actorSelection(RootActorPath(member.address) / "user" / "Frontend")
      frontend ! BackendRegistration
      frontend ! Worker.TransformationJob("dankmeme")
      frontend ! CastRay(self, 1, Ray(Point(1,1,1),Vect(1,1,1)))
      organizer ! GeometryOrganizerAll.CastRay(self, 1, Ray(Point(1,1,1),Vect(1,1,1)))
    }
}
//#worker
object Worker {
  final case class TransformationJob(text: String) extends CborSerializable
  final case class TransformationResult(text: String) extends CborSerializable
  final case class JobFailed(reason: String, job: TransformationJob) extends CborSerializable
  final case class CastRay(recipient: ActorRef, k: Long, r: Ray) extends CborSerializable
  case object BackendRegistration extends CborSerializable
}