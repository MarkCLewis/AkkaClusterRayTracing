package stresser.remoting

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object StartupRemote extends App {
  val system = ActorSystem("HelloRemoteSystem")
  val config = ConfigFactory.load("remote")
}