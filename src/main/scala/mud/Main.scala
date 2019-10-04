package mud

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
  val lights = List(AmbientLight(new RTColor(0.1, 0.1, 0.0, 1.0)), PointLight(new RTColor(0.9,0.9,0.9,1), Point(1e-1, 0, 1e-2)))
  val bimg = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB)
  val img = new rendersim.RTBufferedImage(bimg)
  val numRays = 5
  val aspect = img.width.toDouble / img.height
  val eye = Point(0, 0, 1e-5)
  val topLeft = Point(-1e-5, 1e-5, 0.0)
  val right = Vect(2e-5, 0, 0)
  val down = Vect(0, -2e-5, 0)

  val imageDrawer = system.actorOf(Props(new ImageDrawer(geom, lights, img, numRays)), "imgDraw")
  implicit val timeout = Timeout(100.seconds)
  implicit val ec = system.dispatcher
  
  val frame = new MainFrame {
    title = "Trace Frame"
    contents = new Label("", Swing.Icon(bimg), Alignment.Center)
  }
  frame.visible = true
  
  val fs = for (i <- (0 until img.width); j <- (0 until img.height)) yield {
    (0 until numRays).map(index => {
        imageDrawer ! ImageDrawer.CastRay(i, j, Ray(eye, topLeft + right * (aspect * (i + (if (index > 0) math.random * 0.75 else 0)) / img.width) + down * (j + (if (index > 0) math.random * 0.75 else 0)) / img.height)
        )}
        )
  }
  var now = System.nanoTime()
  var time = System.nanoTime() - now
  while(true) {
    if(time >= (.5 * 1e9)) {
      frame.repaint()
      now = System.nanoTime()
    }
  }
}
