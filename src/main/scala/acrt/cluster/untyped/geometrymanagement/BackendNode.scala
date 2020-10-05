package acrt.cluster.untyped.geometrymanagement

import akka.actor._
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory

object BackendNode extends App {
  val port1 = 5152
  val port2 = 5153

  val list: List[Address] = List(Address("akka", "RTCluster", "127.0.0.1", port1), 
  Address("akka", "RTCluster", "127.0.0.1", port2))

  //def main(args: Array[String]): Unit = {
    // starting 2 frontend nodes and 3 backend nodes
    //require(args.length == 2, "Usage: ip port path")
    //startup("backend", args(0), args(1).toInt, "")
    startup("backend", "127.0.0.1", 5152, "")
  //}

  def startup(role: String, ip: String, port: Int, path: String): Unit = {
    // Override the configuration of the port and role
    val config = ConfigFactory
      .parseString(s"""
        akka.remote.artery.canonical.hostname = "$ip"
        akka.remote.artery.canonical.port=$port
        akka.cluster.roles = [$role]
        """)
      .withFallback(ConfigFactory.load("backend"))

    val system = ActorSystem("RTCluster", config)
    val cluster = Cluster(system)
    val organizer = system.actorOf(Props(new GeometryOrganizerAll(path)), "GeometryOrganizer")
    cluster.joinSeedNodes(list)
  }
}
