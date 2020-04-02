package acrt

import swiftvis2.raytrace._
import data.CartAndRad
import java.awt.image.BufferedImage
import scala.swing._
import java.net.URL
import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import akka.cluster.Cluster
import akka.pattern.ask
import akka.util.Timeout
import akka.cluster.ClusterEvent.MemberEvent
import akka.actor._

object MainNode extends App {
    case class idEnvelope(message: Any)
    case class orgEnvelope(message: Any)
    
    val lights: List[PointLight] = List(PointLight(new RTColor(0.9, 0.9, 0.9, 1), Point(1e-1, 0, 1e-2)), PointLight(new RTColor(0.5, 0.4, 0.1, 1), Point(-1e-1, 0, 1e-2)))
    //Creates an RT BufferedImage of the Screen Size
    val (height, width) = (800, 800)
    val bimg = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB)
    val img = new rendersim.RTBufferedImage(bimg)
    //The RayTrace Parameters. Eye is the POV point. TopLeft, Right, and Down together create a plane through which the Eye will send rays. 
    //NumRays is the Rays per Pixel to be average for AA 
    val numRays = 1
    val eye = Point(0, 0, 2e-5)
    val topLeft = Point(-0.5e-5, 1e-5, 1e-5)
    val right = Vect(2e-5, 0, 0)
    val down = Vect(0, -2e-5, 0)
    
    val carURL = "http://www.cs.trinity.edu/~mlewis/Rings/AMNS-Moonlets/Moonlet4/CartAndRad.6029.bin"
    val commonConfig = ConfigFactory.parseString(
    """
      akka {
        actor.provider = cluster
        remote.artery.enabled = true
        remote.artery.canonical.hostname = 127.0.0.1
        cluster.seed-nodes = [ "akka://cluster@127.0.0.1:25520", "akka://cluster@127.0.0.1:25521" ]
        cluster.jmx.multi-mbeans-in-same-jvm = on
        cluster.failure-detector {
          heartbeat-interval = 3s
          acceptable-heartbeat-pause = 200s
        }
      }
    """)

    def portConfig(port: Int) = ConfigFactory.parseString(s"akka.remote.artery.canonical.port = $port")
    

  val node1 = ActorSystem("cluster", portConfig(25520).withFallback(commonConfig))
  val loggingActor = node1.actorOf(ClusterLogger.props(), "logging-actor")
  val mainNodeActor = node1.actorOf(Props[MainNode], "logging-actor")
  println("")
  println("")
  println("")
  println("")
  println("")
  println("")
  println("")
  println("")
  println("")
  println("")
  println("")
  println("")
  println("")

  mainNodeActor ! idEnvelope(ImageDrawer.Start(eye, topLeft, right, down))
  //Creates the Swing frame and places the Buffered Image in it
  
  Cluster(node1).subscribe(loggingActor, classOf[MemberEvent])

  val node2 = ActorSystem("cluster", portConfig(25521).withFallback(commonConfig))
  val node3 = ActorSystem("cluster", portConfig(25522).withFallback(commonConfig))

  val frame = new MainFrame {
    title = "AkkaRT Frame"
    contents = new Label("", Swing.Icon(bimg), Alignment.Center)
  }
  frame.visible = true
  //Simple repainting timer
  var repainting = true
  var last = System.nanoTime()
  while (true) {
    val delay = System.nanoTime() - last
    if (delay >= (.5 * 1e9)) {
      frame.repaint()
      last = System.nanoTime()
    }
  }
  //Pulls the geometry data from the supplied file within the given directory. Assigns the color of the spheres to black.
  //Creates a List of PointLights. Does not work for AmbientLights.
  //Creates an actorsystem, an ImageDrawer actor to handle RayTracing, and a GeometryManager actor to handle intersection math, then sends the ImageDrawer the message to start
}

class MainNode extends Actor {
  import MainNode._

  private val organizer = context.actorOf(Props(new GeometryOrganizerFew(carURL)), "GeomOrganizer")
  private val imageDrawer = node1.actorOf(Props(new ImageDrawer(lights, img, numRays)), "ImageDrawer")
  
  def receive = {
    case orgEnvelope(msg) => {
      if(organizer != null) {
        organizer ! msg
      } else {
        self ! orgEnvelope(msg)
      }
    }

    case idEnvelope(msg) => {
      if(imageDrawer != null) {
        imageDrawer ! msg
      } else {
        self ! idEnvelope(msg)
      }
    }
  }

}
