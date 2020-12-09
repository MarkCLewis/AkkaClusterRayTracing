package acrt.photometry.untyped

import swiftvis2.raytrace._
import java.awt.image.BufferedImage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
//import scala.swing._
//import ExtendedSlidingBoxSims.SimSpec
import javax.imageio._
import scala.swing.{MainFrame, Label, Swing, Alignment}
import java.net.URL
import data.CartAndRad
import data.HighVelocityCollisions
import java.io._
import akka.actor.ActorSystem
import akka.actor.Props
import acrt.geometrymanagement.untyped.GeometryOrganizerFew
import acrt.geometrymanagement.untyped.GeometryOrganizerAll

//i*i*10
object Main extends App {
    val carURL = new URL("http://www.cs.trinity.edu/~mlewis/Rings/AMNS-Moonlets/Moonlet4/CartAndRad.6029.bin")
    val particles = CartAndRad.readStream(carURL.openStream).map(p => new ScatterSphereGeom(Point(p.x, p.y, p.z), p.rad, _ => new RTColor(1, 1, 1, 1), _ => 0.0))
    
    println("loaded")
    val numRays = 1
    val cellWidth = 1e-5
    val distanceUp = 1e-5
    val viewSize = 1e-5
    val numSims = 6
    val firstXOffset = cellWidth * (numSims - 1)
    val ringGeom = new KDTreeGeometry[BoundingBox](data.CartAndRad.readStream(carURL.openStream)
      .filter(p => p.y < 3e-5 && p.y > -3e-5)
      .map(p => new ScatterSphereGeom(Point(p.x, p.y, p.z), p.rad, _ => new RTColor(1, 1, 1, 1), _ => 0.0)), 5, BoxBoundsBuilder)

    val geom = new ListScene(ringGeom)
    val lights = List(PhotonSource(PointLight(RTColor(1, 1, 1), Point(1, 0, 0.2), Set.empty), 500000), PhotonSource(PointLight(new RTColor(1.0, 0.8, 0.2), Point(-1e-1, 0, 1e-2)), 500000))
    val viewLoc = Point(0, 0, 3e-5)
    val forward = Vect(0, 0, -1)
    val up = Vect(0, 1, 0)
    val bimg = new BufferedImage(1200, 1200, BufferedImage.TYPE_INT_ARGB)
    val img = new rendersim.RTBufferedImage(bimg)

    val system = ActorSystem("AkkaSystem")  
    val organizer = system.actorOf(Props(new GeometryOrganizerAll(particles)), "GeomOrganizer")
    val imageDrawer = system.actorOf(Props(new ImageDrawer(lights, viewLoc, forward, up, img)), "ImageDrawer")

    imageDrawer ! ImageDrawer.AcquireBounds

    val frame = new MainFrame {
      title = "Photometry Frame"
      contents = new Label("", Swing.Icon(bimg), Alignment.Center)
    }
    frame.visible = true
    
    //Simple repainting loop
    var repainting = true
    var last = System.nanoTime()
    while (true) {;
    
    val delay = System.nanoTime() - last
    if (delay >= (.5 * 1e9)) {
      frame.repaint()
      last = System.nanoTime()
    }
  }
}
