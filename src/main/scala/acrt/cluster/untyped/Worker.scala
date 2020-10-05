package acrt.cluster.untyped

import akka.actor._
import akka.cluster._
import akka.cluster.ClusterEvent._

//#worker
class Worker(cluster: Cluster) extends Actor {
  import Worker._
  var frontend: ActorSelection = null
    // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case TransformationJob(text) => frontend ! TransformationResult(text.toUpperCase)
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up).foreach(register)
    case MemberUp(m) => register(m)
  }

  def register(member: Member): Unit =
    if (member.hasRole("frontend")) {
      println(RootActorPath(member.address))
      val port2 = Main.port2
      frontend = context.actorSelection(s"akka://ClusterSystem@127.0.0.1:$port2/user/Frontend")
      frontend ! BackendRegistration
      frontend ! Worker.TransformationJob("dankmeme")
    }
}
//#worker
object Worker {
  final case class TransformationJob(text: String) extends CborSerializable
  final case class TransformationResult(text: String) extends CborSerializable
  final case class JobFailed(reason: String, job: TransformationJob) extends CborSerializable
  case object BackendRegistration extends CborSerializable
}