package acrt.cluster.untyped

import akka.actor._
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import swiftvis2.raytrace._
import java.awt.image.BufferedImage
import scala.swing.MainFrame
import scala.swing.Label
import scala.swing.Swing
import scala.swing.Alignment

object Main {
  val hosts = List("pandora02", "pandora03")
  val ports = List(25251, 25251)

  val list: List[Address] = List(Address("akka", "ClusterSystem", hosts(0), ports(0)), 
  Address("akka", "ClusterSystem", hosts(1), ports(1)))

  def main(args: Array[String]): Unit = {
    // starting 2 frontend nodes and 3 backend nodes
    if (args.isEmpty) {
      startup("backend", "127.0.0.1", ports(0), "0")
      startup("frontend", "127.0.0.1", ports(1), "0")
      } else {
      require((args.length == 3) || (args.length == 4), "Usage: role ip port (number)")
      if(args.length == 3)
        startup(args(0), args(1), args(2).toInt, "0")
      else
        startup(args(0), args(1), args(2).toInt, args(3))
    }
  }

  def startup(role: String, ip: String, port: Int, n: String): Unit = {
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
      system.actorOf(Props(new GeometryManager(cluster, n)), "Worker")
      cluster.joinSeedNodes(list)
    }
    if (role == "frontend") {
      val numRays = 1
      
      val lights: List[PointLight] = List(PointLight(new RTColor(0.9, 0.9, 0.9, 1), Point(1e-1, 0, 1e-2)), PointLight(new RTColor(0.5, 0.4, 0.1, 1), Point(-1e-1, 0, 1e-2)))

      val bimg = new BufferedImage(1200, 1200, BufferedImage.TYPE_INT_ARGB)
      val img = new rendersim.RTBufferedImage(bimg)
        
      val frame = new MainFrame {
        title = "AkkaRT Frame"
        contents = new Label("", Swing.Icon(bimg), Alignment.Center)
      }
      frame.visible = true
      
      var repainting = true
      var last = System.nanoTime()
      val frontend = system.actorOf(Props(new Frontend(img, numRays, lights)), "Frontend")

      cluster.joinSeedNodes(list)

      while (true) {
        val delay = System.nanoTime() - last
        if (delay >= (.5 * 1e9)) {
          frame.repaint()
          last = System.nanoTime()
        }
      }
    }
  }
}