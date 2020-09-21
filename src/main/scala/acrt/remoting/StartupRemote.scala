package acrt.remoting

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object StartupRemote extends App {
  val config = ConfigFactory.load("arterylocal")
  val system = ActorSystem("HelloRemoteSystem", config)
}
