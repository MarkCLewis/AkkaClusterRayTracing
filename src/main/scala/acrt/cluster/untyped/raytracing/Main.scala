package acrt.cluster.untyped.raytracing

import java.awt.image.BufferedImage
import java.net.URL
import data.CartAndRad
import scala.swing.{MainFrame, Label, Swing, Alignment}
import akka.actor.{ActorSystem, Props}
import swiftvis2.raytrace.{PointLight, GeomSphere, RTColor, Point, Vect}
import acrt.geometrymanagement.untyped.{GeometryOrganizerAll, GeometryOrganizerFew, GeometryOrganizerSome}
import akka.actor.Address
import akka.cluster._
import com.typesafe.config.ConfigFactory

object Main extends App {
  val port1 = 5152
  val port2 = 5153

  val list: List[Address] = List(Address("akka", "RTCluster", "127.0.0.1", port1), 
  Address("akka", "RTCluster", "127.0.0.1", port2))
  
  //Visualization Params
  val numRays = 1
  val cellWidth = 1e-5
  val distanceUp = 1e-5
  val viewSize = 1e-5
  val numSims = 6
  val firstXOffset = cellWidth * (numSims - 1)
  
  //View Options: Uncomment for different default views
  //Top-Down view
  val eye = Point(0, 0, distanceUp)
  val topLeft = Point(-viewSize, viewSize, distanceUp - viewSize)
  val right = Vect(2 * viewSize, 0, 0)
  val down = Vect(0, -2 * viewSize, 0)
  
  //Across the top view positive Y
  //val eye = Point(0, -firstXOffset-2*cellWidth, distanceUp)
  //val topLeft = Point(-viewSize, -firstXOffset-2*cellWidth+viewSize, distanceUp + viewSize)
  //val right = Vect(2 * viewSize, 0, 0)
  //val down = Vect(0, 0, -2 * viewSize)

  //Across the top view positive X
  //val eye = Point(-firstXOffset-2*cellWidth, 0, distanceUp)
  //val topLeft = Point(-firstXOffset-2*cellWidth+viewSize, viewSize, distanceUp + viewSize)
  //val right = Vect(0, -2 * viewSize, 0)
  //val down = Vect(0, 0, -2 * viewSize)
  
  //Uncomment for multiple simulations
  //val particles = (0 until numSims).flatMap { i =>
  //  (CartAndRad.read(new java.io.File(s"/home/mlewis/Rings/AMNS-Moonlets/Moonlet4c/CartAndRad.720$i.bin"))).map(p => GeomSphere(Point(p.x - firstXOffset + i * 2 * cellWidth, p.y, p.z), p.rad, _ => new RTColor(1, 1, 1, 1), _ => 0.0))
  //}
  //Creates a List of PointLights. Does not work for AmbientLights.
  val lights: List[PointLight] = List(PointLight(new RTColor(0.9, 0.9, 0.9, 1), Point(1e-1, 0, 1e-2)), PointLight(new RTColor(0.5, 0.4, 0.1, 1), Point(-1e-1, 0, 1e-2)))
  
  //Creates an RT BufferedImage of the Screen Size
  val bimg = new BufferedImage(1200, 1200, BufferedImage.TYPE_INT_ARGB)
  val img = new rendersim.RTBufferedImage(bimg)
    
  //Creates an actorsystem, an ImageDrawer actor to handle RayTracing, and a GeometryManager actor to handle intersection math, then sends the ImageDrawer the message to start
  val config = ConfigFactory
      .parseString(s"""
        akka.remote.artery.canonical.hostname = "127.0.0.1"
        akka.remote.artery.canonical.port=$port2
        akka.cluster.roles = [frontend]
        """)
      .withFallback(ConfigFactory.load("frontend"))
  val system = ActorSystem("RTCluster", config)  
  val cluster = Cluster(system)
  val imageDrawer = system.actorOf(Props(new ImageDrawer(lights, img, numRays)), "ImageDrawer")
  imageDrawer ! ImageDrawer.Start(eye, topLeft, right, down)
  
  cluster.joinSeedNodes(list)

  //Creates the Swing frame and places the Buffered Image in it
  val frame = new MainFrame {
    title = "AkkaRT Frame"
    contents = new Label("", Swing.Icon(bimg), Alignment.Center)
  }
  frame.visible = true
  
  //Simple repainting loop
  var repainting = true
  var last = System.nanoTime()
  while (true) {
    val delay = System.nanoTime() - last
    if (delay >= (.5 * 1e9)) {
      frame.repaint()
      last = System.nanoTime()
    }
  }
}
