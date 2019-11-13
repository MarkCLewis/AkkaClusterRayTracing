package acrt

import swiftvis2.raytrace._
import data.CartAndRad
import swiftvis2.raytrace.LinearViewPath._
import java.awt.image.BufferedImage
import scala.swing._
import swiftvis2._
import java.io.File
import ExtendedSlidingBoxSims.SimSpec
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.IIOImage
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
import akka.actor.ActorRef
import scala.concurrent.Future

object Main extends App {
  val system = ActorSystem("MUDSystem")
  val dir = new File("/users/mlewis/Local/HTML-Documents/Rings/AMNS-Moonlets/Moonlet4")
  val carFile = new File(dir, "CartAndRad.6029.bin")
  val particles = CartAndRad.read(carFile).map(p => GeomSphere(Point(p.x, p.y, p.z), p.rad, _ => new RTColor(1, 1, 1, 1), _ => 0.0))
  println(particles.maxBy(_.radius))
  println(particles.maxBy(_.center.x))
  println(particles.maxBy(_.center.y))
  // sys.exit(0)
  val geom = new KDTreeGeometry(particles)
  val lights = List(/*AmbientLight(new RTColor(0.1, 0.1, 0.0, 1.0)),*/ PointLight(new RTColor(0.9, 0.9, 0.9, 1), Point(1e-1, 0, 1e-2)))
  val bimg = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB)
  val img = new rendersim.RTBufferedImage(bimg)
  val numRays = 5 // TODO: Make code work with this!!
  val eye = Point(0, 0, 1e-5)
  val topLeft = Point(-1e-5, 1e-5, 0.0)
  val right = Vect(2e-5, 0, 0)
  val down = Vect(0, -2e-5, 0)

  val imageDrawer = system.actorOf(Props(new ImageDrawer(geom, lights, img, numRays)), "imgDraw")
  val manager: ActorRef = system.actorOf(Props(new RTManager(geom, lights, numRays)), "RTMan")
  implicit val timeout = Timeout(100.seconds)
  implicit val ec = system.dispatcher
  imageDrawer ! ImageDrawer.Start(eye, topLeft, right, down)

  val frame = new MainFrame {
    title = "AkkaRT Frame"
    contents = new Label("", Swing.Icon(bimg), Alignment.Center)
  }
  frame.visible = true

  var last = System.nanoTime()
  while (true) {
    val delay = System.nanoTime() - last
    if (delay >= (.5 * 1e9)) {
      println("repainting")
      frame.repaint()
      last = System.nanoTime()
    }
  }
}
