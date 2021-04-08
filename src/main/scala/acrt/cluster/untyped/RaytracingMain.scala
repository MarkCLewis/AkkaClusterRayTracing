package acrt.cluster.untyped

import java.awt.image.BufferedImage
import scala.swing.{MainFrame, Label, Swing, Alignment}
import akka.actor.{Actor, Address, ActorSystem, Props}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import swiftvis2.raytrace.{Point, PointLight, RTColor}
import backend.BackendNode
import frontend.raytracing.RTFrontendNode
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import acrt.cluster.untyped.frontend.FrontendNode

object RaytracingMain {
  //Swap to change transport between UDP and TCP
  val transport = "tcp"
  //val transport = "aeron-udp"

  //Uncomment to use all pandora machines
  val hosts = List(
    "janus00", "janus01", "janus02", "janus03", "janus04", 
    "janus05", "janus06", "janus07", "janus08", "janus09",
    "janus10", "janus11", "janus12", "janus13", "janus14", 
    "janus15", "janus16", "janus17", "janus18", "janus19",
    "janus20", "janus21", "janus22", "janus23", "janus24"
  )
  //val hosts = List("pandora00", "pandora01", "pandora02", "pandora03", "pandora04", "pandora05", "pandora06", "pandora07", "pandora08")
  //val hosts = List("pandora02", "pandora03")
  val port = 25251
  val list = hosts.map(Address("akka", "ClusterSystem", _, port))

  //Uncomment to use locally
  /*val hosts = "pandora02"
  val port = List(25251, 25252)
  val list = port.map(Address("akka", "ClusterSystem", hosts, _))*/

  //Reads args and starts up BackendNodes and FrontendNodes
  def main(args: Array[String]): Unit = {
    //If no args, works locally
    if (args.isEmpty) {
      startup("backend", "pandora02", 25251, "0")
      startup("frontend", "pandora02", 25252, "0")
      } else {
      require((args.length == 3) || (args.length == 4), "Usage: role ip port (number)")
      //If 3 args, must be frontend, and starts up frontend
      if(args.length == 3)
        startup(args(0), args(1), args(2).toInt, "0")
      //If 4 args, must be backend, and starts up backend
        else
        startup(args(0), args(1), args(2).toInt, args(3))
    }
  }

  //Starts up backend or frontend nodes based on the args passed in
  def startup(role: String, ip: String, port: Int, n: String): Unit = {
    //Makes ip, port, transport, and role into the config. Edit the loaded fallback to change serializer
    //Options: jacksonserialize, kryoserialize, javaserialize
    val config = ConfigFactory
      .parseString(s"""
        akka.remote.artery.canonical.hostname = "$ip"
        akka.remote.artery.canonical.port=$port
        akka.remote.artery.canonical.transport=$transport
        akka.cluster.roles = [$role]
        """)
      .withFallback(ConfigFactory.load("kryoserialize"))

    //Creates the cluster and system with the config
    val system = ActorSystem("ClusterSystem", config)
    val cluster = Cluster(system)
    
    //If backend, creates a new BackendNode in the cluster, then joins
    if (role == "backend") {
      system.actorOf(Props(new BackendNode(cluster, n.toInt)), "Worker")
      cluster.joinSeedNodes(list)
    }
    
    //If frontend, starts up FrontendNode, with the swing image, and then starts repainting
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
      val frontend = system.actorOf(Props(new RTFrontendNode(img, numRays, lights)), "Frontend")

      cluster.joinSeedNodes(list)

      frame.peer.addWindowListener(new WindowAdapter{
        override def windowClosing(e: WindowEvent) = {
          frontend ! FrontendNode.KillCluster
        }
      })

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
