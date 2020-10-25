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
  //val hosts = List("pandora01", "pandora02", "pandora03", "pandora04", "pandora05", "pandora06", "pandora07", "pandora08")
  val hosts = List("pandora02", "pandora03")
  val port = 25251

  val list = hosts.map(Address("akka", "ClusterSystem", _, port))

  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      startup("backend", "127.0.0.1", 25251, "0")
      startup("frontend", "127.0.0.1", 25252, "0")
      } else {
      require((args.length == 3) || (args.length == 4), "Usage: role ip port (number)")
      if(args.length == 3)
        startup(args(0), args(1), args(2).toInt, "0")
      else
        startup(args(0), args(1), args(2).toInt, args(3))
    }
  }

  def startup(role: String, ip: String, port: Int, n: String): Unit = {
    val config = ConfigFactory
      .parseString(s"""
        akka.remote.artery.canonical.hostname = "$ip"
        akka.remote.artery.canonical.port=$port
        akka.cluster.roles = [$role]
        """)
      .withFallback(ConfigFactory.load("application"))

    val system = ActorSystem("ClusterSystem", config)
    val cluster = Cluster(system)
    
    if (role == "backend") {
      system.actorOf(Props(new Backend(cluster, n.toInt)), "Worker")
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