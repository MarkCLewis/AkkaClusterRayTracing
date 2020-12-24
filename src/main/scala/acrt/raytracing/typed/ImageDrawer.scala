package acrt.raytracing.typed

import akka.actor.{Actor, Props}
import swiftvis2.raytrace.{PointLight, Ray, Point, Vect, RTColor}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, ActorContext}
import acrt.geometrymanagement.typed.{GeometryOrganizer}
import swiftvis2.raytrace.Geometry
import acrt.geometrymanagement.untyped.GeometryOrganizerAll

//UNFINISHED

/*class ImageDrawer(lights: List[PointLight], img: rendersim.RTBufferedImage, numRays: Int) extends Actor {
  import ImageDrawer._

  def receive = {
    

    case SetColor(i, j, color) => {
      //Assigns the (x,y) pixel of the BufferedImage to be the supplied color
      img.setColor(i, j, color)
      
      //Checks to see if the image is completed and ends time
      pixelsSet += 1
      if (pixelsSet >= totalPixels) {
        println((System.nanoTime() - start) * 1e-9)
      }
    }

    case m => "ImageDrawer received unhandled message: " + m
  }
}*/

object ImageDrawer {
  sealed trait ImageWork
  case class Start(eye: Point, topLeft: Point, right: Vect, down: Vect) extends ImageWork
  case class SetColor(i: Int, j: Int, color: RTColor) extends ImageWork

  
  //val aspect = img.width.toDouble / img.height
  
  private var pixelsSet = 0
  //private val totalPixels = img.width * img.height * numRays
  private val start = System.nanoTime()

  def apply(lights: List[PointLight], img: rendersim.RTBufferedImage, numRays: Int, geomOrg: ActorRef[GeometryOrganizer.CastRay]): Behavior[ImageWork] = Behaviors.receive { (context, message) => 
    message match {
      case Start(eye, topLeft, right, down) => {
        for (i <- (0 until img.width); j <- (0 until img.height)) {
          val pix = context.spawn(PixelHandler(lights, i, j, numRays, context.self), s"PixelHandler$i,$j")
          val aspect = 4
          (0 until numRays).map(index => {
            pix ! PixelHandler.AddRay(Ray(eye, topLeft + right * (aspect * (i + (if (index > 0) math.random * 0.75 else 0)) / img.width) + down * (j + (if (index > 0) math.random * 0.75 else 0)) / img.height), geomOrg)
          })
        }
      }
      case _ =>
    }
    Behaviors.same
  }
}