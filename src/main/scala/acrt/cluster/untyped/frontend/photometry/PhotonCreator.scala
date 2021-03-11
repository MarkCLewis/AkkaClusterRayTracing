package acrt.cluster.untyped.frontend.photometry

import scala.collection.mutable
import akka.actor.{Actor, Props, ActorRef}
import swiftvis2.raytrace.{PointLight, RTColor, Ray}
import acrt.cluster.untyped.frontend.raytracing.PixelHandler
import acrt.cluster.untyped.backend.{IntersectContainer, CborSerializable}
import swiftvis2.raytrace.Vect
import swiftvis2.raytrace.Point

class PhotonCreator(xmin: Double, xmax: Double, ymin: Double, ymax: Double, source: PhotonSource, viewLoc: Point, forward: Vect, up: Vect, image: rendersim.RTBufferedImage, organizer: ActorRef) extends Actor {
  import PhotonCreator._

  val right = forward.cross(up)
  var pixels = Array.fill(image.width, image.height)(RTColor.Black)
  val rays = collection.mutable.Map[Long, Ray]() 
  private var returnedRays = 0L
  
  def receive = {
    case SetColor(x, y, col) => {
      context.parent ! ImageDrawer.UpdateColor(x, y, pixels(x)(y) + col)
      pixels(x)(y) = pixels(x)(y) + col
    }
    case PixelHandler.IntersectResult(k, oid) => {
      oid.foreach { iContainer =>
        val newScatterer = context.actorOf(Props(new Scatterer(source, viewLoc, forward, up, iContainer, image.width, image.height, rays(k).dir, organizer)), s"Scatterer$k")
        //println(s"Ray $k was Scattered")
        }
    }
    
    
    case Render => {
      for (_ <- 0L until source.numPhotons) {
        val ray = Ray(
          source.light.point,
            Point(
              xmin + math.random() * (xmax - xmin),
              ymin + math.random() * (ymax - ymin),
              0.0
            )
        )
        val k = scala.util.Random.nextLong()
        organizer ! GeometryOrganizerAll.CastRay(self, k, ray)
        rays += (k -> ray)
      }
    }
    
    case m => "me photoncreator. me receive " + m
  }
}
object PhotonCreator {
  case object Render extends CborSerializable
  case class SetColor(x: Int, y: Int, col: RTColor) extends CborSerializable
}
