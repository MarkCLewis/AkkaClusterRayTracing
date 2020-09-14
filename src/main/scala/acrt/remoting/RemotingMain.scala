package acrt.remoting

import akka.actor._
import com.typesafe.config.ConfigFactory

object RemotingMain extends App {
  val config = ConfigFactory.load("remote")

  val system = ActorSystem("TestSystem", config)
  val tikActor = system.actorOf(Props[TikActor], "TikActor")
  //val tokActor = system.actorOf(Props[TokActor], "TokActor")
  tikActor ! "start"
}
