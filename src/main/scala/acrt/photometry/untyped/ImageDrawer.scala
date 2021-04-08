package acrt.photometry.untyped

import akka.actor.Actor
import swiftvis2.raytrace.RTColor
import swiftvis2.raytrace.Geometry
import swiftvis2.raytrace.Point
import swiftvis2.raytrace.Vect
import swiftvis2.raytrace.RTImage
import collection.mutable
import akka.actor.Props
import acrt.geometrymanagement.untyped.GeometryOrganizer

class ImageDrawer(sources: List[PhotonSource], viewLoc: Point, forward: Vect, up: Vect, img: RTImage) extends Actor {
    import ImageDrawer._

    private var howManyRays = sources.map(m => m.numPhotons).sum
    val threads = 8
    private var pixels: Array[Array[RTColor]] = null

    private var xmin = 0.0
    private var xmax = 0.0
    private var ymin = 0.0
    private var ymax = 0.0    
    
    private var changedPixels = 0

    def receive = {
      case MoreRays => {
        howManyRays += 1
      }

      case AcquireBounds => {
        Main.organizer ! GeometryOrganizer.GetBounds(self)
        println("getting bounds")
      }

      case Bounds(x1, x2, y1, y2) => {
        xmin = x1
        xmax = x2
        ymin = y1
        ymax = y2  

        println(s"$xmin, $xmax, $ymin, $ymax")
        val startPixels = Array.fill(img.width, img.height)(RTColor.Black)
        self ! ImageDrawer.Start(startPixels)
      }

      case Start(startPixels) => {
        println("starting")
        pixels = startPixels

        for(c <- 1 to threads; light <- sources) {
            val id = light.numPhotons + scala.util.Random.nextLong()
            val child = context.actorOf(Props(new PhotonCreator(xmin, xmax, ymin, ymax, light, viewLoc, forward, up, img)), s"PhotonSender$c,$id")
            child ! PhotonCreator.Render
        }
      }
      case UpdateColor(x, y, col) => {
        changedPixels += 1
        howManyRays -= 1

        val startingColor = pixels(x)(y)

        if(col.a != 0.0 || col.b != 0.0 || col.g != 0.0 || col.r != 0.0) pixels(x)(y) = col + startingColor

        if(changedPixels >= 100 /*img.width * img.height*/) {
          writeToImage(pixels, img)
          changedPixels = 0
        }

        if(howManyRays <= 0) println("Drew all rays")
      }
      case m => "me imagedrawer. me receive " + m
    }

  def writeToImage(pixels: Array[Array[RTColor]], image: RTImage): Unit = {
    val maxPix = pixels.foldLeft(0.0)((m,row) => m max row.foldLeft(0.0)((m2, p) => m2 max p.r max p.g max p.b))
    for (px <- 0 until image.width; py <- 0 until image.height) {
      image.setColor(px, py, (pixels(px)(py) / maxPix * 2.0).copy(a = 1.0))
    }
  }
}

object ImageDrawer {
    case object MoreRays
    case class Start(startPixels: Array[Array[RTColor]])
    case class UpdateColor(x: Int, y: Int, col: RTColor)
    case class Bounds(xmin: Double, xmax: Double, ymin: Double, ymax: Double)
    case object AcquireBounds
}
