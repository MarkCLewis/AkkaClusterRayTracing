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
  val geom = new GeomSphere(Point(0, 0, 0), 2, _ => new RTColor(1, 1, 1, 1), _ => 0.0)
  val lights = List(AmbientLight(new RTColor(0.1, 0.0, 0.0, 1.0)), PointLight(new RTColor(1,1,1,1), Point(0, 0, 10)))
  val manager:ActorRef = system.actorOf(Props(new RTManager(geom, lights)), "RTMan")
  val bimg = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB)
  val img = new rendersim.RTBufferedImage(bimg)
  val numRays = 5
  val aspect = img.width.toDouble / img.height
  val eye = Point(10, 0, 0)
  val topLeft = Point(9, -1, -1)
  val right = Vect(0, 2, 0)
  val down = Vect(0, 0, 2)
  implicit val timeout = Timeout(100.seconds)
  implicit val ec = system.dispatcher
  val frame = new MainFrame {
    title = "Trace Frame"
    contents = new Label("", Swing.Icon(bimg), Alignment.Center)
  }
  frame.visible = true
  val fs = for (i <- (0 until img.width); j <- (0 until img.height)) yield {
    val rayFutures = (0 until numRays).map(index => {
        val future =  manager ask RTManager.CastRay(
            Ray(eye, topLeft + right * (aspect * (i + (if (index > 0) math.random * 0.75 else 0)) / img.width) + down * (j + (if (index > 0) math.random * 0.75 else 0)) / img.height)
          )
          future
        }
        )
    (i, j, Future.sequence(rayFutures))
  }
  val doneFuture = Future.sequence(fs.map { case (i, j, fc) => 
    //ignore warning, should be safe for now
    fc.map { case colors:Seq[RTColor] => 
      img.setColor(i, j, colors.reduceLeft(_ + _) / numRays) 
      frame.repaint()
    }
  })
  doneFuture.foreach { _ =>
    println("Done")
    frame.repaint()
  }
}
