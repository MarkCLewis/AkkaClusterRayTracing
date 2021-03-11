package acrt.cluster.untyped.frontend.photometry

import akka.actor.{Actor, Props, ActorRef}
import swiftvis2.raytrace.{PointLight, Ray, Point, Vect, RTColor}
import acrt.cluster.untyped.backend.CborSerializable

class ImageDrawer(sources: List[PhotonSource], img: rendersim.RTBufferedImage, numRays: Int, organizer: ActorRef) extends Actor {
  import ImageDrawer._
  
  val viewLoc = Point(0, 0, 3e-5)
  val forward = Vect(0, 0, -1)
  val up = Vect(0, 1, 0)

  private var howManyRays = sources.map(m => m.numPhotons).sum
  val threads = 8
  private var pixels: Array[Array[RTColor]] = null

  private var xmin = 0.0
  private var xmax = 0.0
  private var ymin = 0.0
  private var ymax = 0.0    
    
  private var changedPixels = 0

  val aspect = img.width.toDouble / img.height
  private var pixelsSet = 0
  private val totalPixels = img.width * img.height * numRays

  private var startTime: Long = 0

  def receive = {
    case MoreRays => {
        howManyRays += 1
    }

    case AcquireBounds => {
        organizer ! GeometryOrganizerAll.GetBounds
    }

    case Bounds(x1, x2, y1, y2) => {
        xmin = x1
        xmax = x2
        ymin = y1
        ymax = y2  

        val startPixels = Array.fill(img.width, img.height)(RTColor.Black)
        self ! ImageDrawer.Start(startPixels)
      }

    //Starts drawing, sends out rays for every pixel
    case Start(startPixels) => {
      pixels = startPixels

      for(c <- 1 to threads; light <- sources) {
          val id = light.numPhotons + scala.util.Random.nextLong()
          val child = context.actorOf(Props(new PhotonCreator(xmin, xmax, ymin, ymax, light, viewLoc, forward, up, img, organizer)), s"PhotonSender$c,$id")
          child ! PhotonCreator.Render
      }
    }

    //Sets color at the given pixel
    case UpdateColor(x, y, col) => {
      changedPixels += 1
      howManyRays -= 1

      val startingColor = pixels(x)(y)
      if(col.a != 0.0 || col.b != 0.0 || col.g != 0.0 || col.r != 0.0) pixels(x)(y) = col + startingColor
      if(changedPixels >= img.width*img.height) {
        writeToImage(pixels, img)
        changedPixels = 0
      }
        
      println("pix" + changedPixels)
      if(howManyRays <= 0) println("Drew all rays")
    }

    case m => "ImageDrawer received unhandled message: " + m
  }

  def writeToImage(pixels: Array[Array[RTColor]], image: rendersim.RTBufferedImage): Unit = {
    val maxPix = pixels.foldLeft(0.0)((m,row) => m max row.foldLeft(0.0)((m2, p) => m2 max p.r max p.g max p.b))
    for (px <- 0 until image.width; py <- 0 until image.height) {
      image.setColor(px, py, (pixels(px)(py) / maxPix * 2.0).copy(a = 1.0))
    }
  }
}

object ImageDrawer {
  case object MoreRays extends CborSerializable
  case class Start(startPixels: Array[Array[RTColor]]) extends CborSerializable
  case class UpdateColor(x: Int, y: Int, col: RTColor) extends CborSerializable
  case class Bounds(xmin: Double, xmax: Double, ymin: Double, ymax: Double) extends CborSerializable
  case object AcquireBounds extends CborSerializable
} 