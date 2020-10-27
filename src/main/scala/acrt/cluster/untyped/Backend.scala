package acrt.cluster.untyped

import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable
import akka.actor.Props
import akka.cluster.Cluster
import akka.actor.RootActorPath
import akka.actor.ActorSelection
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.MemberStatus
import akka.cluster.Member

class Backend(cluster: Cluster, number: Int) extends Actor {
  import Backend._
  private val managers: mutable.Map[String, ActorRef] = mutable.Map()
  private var frontend: ActorSelection = null
  private var organizer: ActorRef = null

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case MakeManager(num, offset) => {
      organizer = sender
      val rand = scala.util.Random.nextLong()
      val mgr = context.actorOf(Props(new GeometryManager(cluster, organizer, num, offset)), s"Manager$rand")
      managers += (num -> mgr)
      mgr ! GeometryManager.OrganizerRegistration
      println("making manager")
    }

    case MemberUp(m) => register(m)

    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up).foreach(register)
  }

  def register(member: Member): Unit =
    if (member.hasRole("frontend")) {
      println(RootActorPath(member.address))
      frontend = context.actorSelection(RootActorPath(member.address) / "user" / "Frontend")
      frontend ! Frontend.BackendRegistration
      while(managers.isEmpty == false) {
        managers -= managers.head._1
      }
    }
}

object Backend {
    case class MakeManager(number: String, offset: Double)
}
