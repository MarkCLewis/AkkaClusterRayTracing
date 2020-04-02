package acrt

import akka.actor.Actor
import swiftvis2.raytrace._
import akka.actor.Props

class ImageDrawer(lights: List[PointLight], img: rendersim.RTBufferedImage, numRays: Int) extends Actor {
  import ImageDrawer._
  //Aspect Ratio  
  val aspect = img.width.toDouble / img.height
  val startTime = System.nanoTime()
  val numPixels: Long = img.height * img.width
  private var pixelCounter = 0
  def receive = {
    case Start(eye, topLeft, right, down) => {
      for (i <- (0 until img.width); j <- (0 until img.height)) {
        //println("start")
        //Creates a new child actor assigned to the given (x,y) pixel
        val pix = context.actorOf(Props(new PixelHandler(lights, i, j, numRays)), s"PixelHandler$i,$j")
        //println("pixelhandler create " + pix)
        //Sends numRays Rays to the new PixelHandler to be sent to the Geometry and averaged into a color
        (0 until numRays).map(index => {
          pix ! PixelHandler.AddRay(Ray(eye, topLeft + right * (aspect * (i + (if (index > 0) math.random * 0.75 else 0)) / img.width) + down * (j + (if (index > 0) math.random * 0.75 else 0)) / img.height))
        })
      }
    }

    case SetColor(i, j, color) => {
      //Assigns the (x,y) pixel of the BufferedImage to be the supplied color
      img.setColor(i, j, color)
      //If set numPixels to 3, inconsistent total?
      pixelCounter += 1
      //if(pixelCounter >= (numPixels * .75)) {
      if(pixelCounter >= numPixels) {
        val howLong = (System.nanoTime - startTime) / 1e9
        println("Completed RayTrace of " + pixelCounter + " out of " + numPixels + ". Took " + howLong + " seconds.")
        //Main.repainting
      }
      println(s"setting color $i $j $color")
    }

    case m => "ImageDrawer received unhandled message: " + m
  }
}
//1468023
object ImageDrawer {
  case class Start(eye: Point, topLeft: Point, right: Vect, down: Vect)
  case class SetColor(i: Int, j: Int, color: RTColor)
}