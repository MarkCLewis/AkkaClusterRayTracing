package acrt.raytracing.typed

import java.awt.image.BufferedImage
import java.net.URL
import data.CartAndRad
import scala.swing.{MainFrame, Label, Swing, Alignment}
import swiftvis2.raytrace.{PointLight, GeomSphere, RTColor, Point, Vect}
import acrt.geometrymanagement.typed.{GeometryOrganizerAll, GeometryOrganizerFew, GeometryOrganizerSome}
import akka.actor.typed.{ActorRef, Behavior, ActorSystem}
import akka.actor.typed.javadsl.Behaviors

object Main {
  def main(args: Array[String]): Unit = {
    val system: ActorSystem[NotUsed] = ActorSystem(Main(),"AkkaSystem") 

  }
  
  sealed trait NotUsed
  def apply(): Behavior[NotUsed] = {
    Behaviors.setup { context =>

      val carURL = new URL("http://www.cs.trinity.edu/~mlewis/Rings/AMNS-Moonlets/Moonlet4/CartAndRad.6029.bin")
      val particles = CartAndRad.readStream(carURL.openStream).map(p => GeomSphere(Point(p.x, p.y, p.z), p.rad, _ => new RTColor(1, 1, 1, 1), _ => 0.0))
  
      val numRays = 1
      val cellWidth = 1e-5
      val distanceUp = 1e-5
      val viewSize = 1e-5
      val numSims = 6
      val firstXOffset = cellWidth * (numSims - 1)
  
      val eye = Point(0, 0, distanceUp)
      val topLeft = Point(-viewSize, viewSize, distanceUp - viewSize)
      val right = Vect(2 * viewSize, 0, 0)
      val down = Vect(0, -2 * viewSize, 0)
  
      println(s"# particles = ${particles.length}")
  
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
      
      val organizer = context.spawn(GeometryOrganizerFew(particles), "GeomOrganizer")
      val imageDrawer = context.spawn(ImageDrawer(lights, img, numRays, organizer), "ImageDrawer")
      
      imageDrawer ! ImageDrawer.Start(eye, topLeft, right, down)

      while (true) {
        val delay = System.nanoTime() - last
        if (delay >= (.5 * 1e9)) {
          frame.repaint()
          last = System.nanoTime()
        }
      }
      
      Behaviors.same
    }
  }
}
