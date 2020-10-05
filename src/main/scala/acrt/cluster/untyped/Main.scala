package acrt.cluster.untyped

import akka.actor._
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory

object Main {
  val port1 = 25278
  val port2 = 25279

  val list: List[Address] = List(Address("akka", "ClusterSystem", "127.0.0.1", port1), 
  Address("akka", "ClusterSystem", "127.0.0.1", port2))

  def main(args: Array[String]): Unit = {
    // starting 2 frontend nodes and 3 backend nodes
    if (args.isEmpty) {
      startup("backend", "127.0.0.1", port1)
      startup("frontend", "127.0.0.1", port2)
      } else {
      require(args.length == 3, "Usage: role ip port")
      startup(args(0), args(1), args(2).toInt)
    }
  }

  def startup(role: String, ip: String, port: Int): Unit = {
    // Override the configuration of the port and role
    val config = ConfigFactory
      .parseString(s"""
        akka.remote.artery.canonical.hostname = "$ip"
        akka.remote.artery.canonical.port=$port
        akka.cluster.roles = [$role]
        """)
      .withFallback(ConfigFactory.load("transformation"))

    val system = ActorSystem("ClusterSystem", config)
    val cluster = Cluster(system)
    
    if (role == "backend") {
        system.actorOf(Props(new Worker(cluster)), "Worker")
    }
    if (role == "frontend") {
        val frontend = system.actorOf(Props[Frontend], "Frontend")
    }

    cluster.joinSeedNodes(list)
  }
}