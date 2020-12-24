package acrt.cluster.typed

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory

object App {
  object RootBehavior {
    def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
      val cluster = Cluster(ctx.system)

      if (cluster.selfMember.hasRole("backend")) {
        val workersPerNode =
          ctx.system.settings.config.getInt("transformation.workers-per-node")
        (1 to workersPerNode).foreach { n =>
          ctx.spawn(Worker(), s"Worker$n")
        }
      }
      if (cluster.selfMember.hasRole("frontend")) {
        ctx.spawn(Frontend(), "Frontend")
      }
      Behaviors.empty
    }
  }

  def main(args: Array[String]): Unit = {
    // starting 2 frontend nodes and 3 backend nodes
    if (args.isEmpty) {
      startup("backend", "127.0.0.1", 25251)
      startup("backend", "127.0.0.1", 25252)
      startup("frontend", "127.0.0.1", 0)
      startup("frontend", "127.0.0.1", 0)
      startup("frontend", "127.0.0.1", 0)
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

    ActorSystem[Nothing](RootBehavior(), "ClusterSystem", config)

  }

}
