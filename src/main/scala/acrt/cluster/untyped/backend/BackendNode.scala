package acrt.cluster.untyped.backend

import scala.collection.mutable
import akka.actor.{Actor, ActorRef, RootActorPath, ActorSelection, Props, PoisonPill}
import akka.cluster.{Cluster, MemberStatus, Member}
import akka.cluster.ClusterEvent.{MemberUp, CurrentClusterState}
import acrt.cluster.untyped.frontend.raytracing.Frontend

class BackendNode(cluster: Cluster, number: Int) extends Actor {
  import Backend._
  private val managers: mutable.Map[String, ActorRef] = mutable.Map()
  private var frontend: ActorSelection = null
  private var organizer: ActorRef = null

  //Subscribe to new members joining the cluster, and unsubscribe if terminated
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    //Makes a new GeometryManager with a random name to contain the data from the given file, with the given x offset
    case MakeManager(num, offset) => {
      println("making manager")
      organizer = sender
      val rand = scala.util.Random.nextLong()
      val mgr = context.actorOf(Props(new GeometryManager(cluster, organizer, num, offset)), s"Manager$rand")
      
      //Adds the manager into the map of managers with its number, then tells the manager to register with the Organizer
      managers += (num -> mgr)
      mgr ! GeometryManager.OrganizerRegistration
    }

    //Upon being alerted of a new member joining cluster, registers with it if its a Frontend
    case MemberUp(m) => register(m)

    //When the state of the cluster changes, i.e. a node is lost, it reregisters with all other nodes
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up).foreach(register)
  }

  //Takes a member and, if the member is a frontend, sends a message to register with it
  def register(member: Member): Unit = {
    if (member.hasRole("frontend")) {
      frontend = context.actorSelection(RootActorPath(member.address) / "user" / "Frontend")
      frontend ! Frontend.BackendRegistration

      //Removes all previous managers from previous runs
      while(managers.isEmpty == false) {
        managers.head._2 ! PoisonPill
        managers -= managers.head._1
      }
    }
  }
}

object Backend {
    case class MakeManager(number: String, offset: Double) extends CborSerializable
}
