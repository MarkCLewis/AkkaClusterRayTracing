package acrt

import swiftvis2.raytrace._
import data.CartAndRad
import java.awt.image.BufferedImage
import scala.swing._
import java.io.File
import akka.actor.ActorSystem
import akka.actor.Props

object Main extends App {
  //Pulls the geometry data from the supplied file within the given directory. Assigns the color of the spheres to black.
  val dir = new File("/users/mlewis/Local/HTML-Documents/Rings/AMNS-Moonlets/Moonlet4")
  val carFile = new File(dir, "CartAndRad.6029.bin")
  val particles = CartAndRad.read(carFile).map(p => GeomSphere(Point(p.x, p.y, p.z), p.rad, _ => new RTColor(1, 1, 1, 1), _ => 0.0))
  val geom = new KDTreeGeometry(particles)
  //Creates a List of PointLights. Does not work for AmbientLights.
  val lights: List[PointLight] = List(PointLight(new RTColor(0.9, 0.9, 0.9, 1), Point(1e-1, 0, 1e-2)), PointLight(new RTColor(0.5, 0.4, 0.1, 1), Point(-1e-1, 0, 1e-2)))
  //Creates an RT BufferedImage of the Screen Size
  val bimg = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB)
  val img = new rendersim.RTBufferedImage(bimg)
  //The RayTrace Parameters. Eye is the POV point. TopLeft, Right, and Down together create a plane through which the Eye will send rays. 
  //NumRays is the Rays per Pixel to be average for AA 
  val numRays = 3
  val eye = Point(0, 0, 1e-5)
  val topLeft = Point(-1e-5, 1e-5, 0.0)
  val right = Vect(2e-5, 0, 0)
  val down = Vect(0, -2e-5, 0)
  //Creates an actorsystem, an ImageDrawer actor to handle RayTracing, and a GeometryManager actor to handle intersection math, then sends the ImageDrawer the message to start
  val system = ActorSystem("RTSystem")  
  val imageDrawer = system.actorOf(Props(new ImageDrawer(geom, lights, img, numRays)), "ImageDrawer")
  val manager = system.actorOf(Props(new GeometryManager(geom)), "GeomManager")
  imageDrawer ! ImageDrawer.Start(eye, topLeft, right, down)
  //Creates the Swing frame and places the Buffered Image in it
  val frame = new MainFrame {
    title = "AkkaRayTrace"
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
}
