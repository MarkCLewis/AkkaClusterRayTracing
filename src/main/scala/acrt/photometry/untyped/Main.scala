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
import acrt.geometrymanagement.untyped.GeometryOrganizerSome
import acrt.geometrymanagement.untyped.PhotometryCreator

//i*i*10
object Main extends App {
    val numRays = 1
    val cellWidth = 1e-5
    val distanceUp = 1e-5
    val viewSize = 1e-5
    val numSims = 10

    val lights = List(PhotonSource(PointLight(RTColor(1, 1, 1), Point(1, 0, 0.2)), 4000))
    val forward = Vect(0, 0, -1)
    val up = Vect(0, 1, 0)
    //val viewLoc = Point(0.0, 0.0, numFiles*1e-5)
    val n = math.sqrt(numSims.toDouble / 10.0).ceil.toInt
    val viewLoc = Point(0.0, 0.0, (10 * n)*1e-5)
    val bimg = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB)
    val img = new rendersim.RTBufferedImage(bimg)

    val pc = new PhotometryCreator

    val system = ActorSystem("AkkaSystem")  
    val organizer = system.actorOf(Props(new GeometryOrganizerSome(numSims, pc)), "GeomOrganizer")
    val imageDrawer = system.actorOf(Props(new ImageDrawer(lights, viewLoc, forward, up, img)), "ImageDrawer")

    imageDrawer ! ImageDrawer.AcquireBounds

    val frame = new MainFrame {
      title = "AkkaPMR Frame"
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
