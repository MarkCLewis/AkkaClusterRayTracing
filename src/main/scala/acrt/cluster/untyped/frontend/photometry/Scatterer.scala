package acrt.cluster.untyped.frontend.photometry

import scala.collection.mutable
import akka.actor.{Actor, ActorRef}
import swiftvis2.raytrace.{PointLight, RTColor, Ray}
import acrt.cluster.untyped.backend.IntersectContainer
import acrt.cluster.untyped.frontend.raytracing.PixelHandler
import swiftvis2.raytrace.Vect
import swiftvis2.raytrace.Point
import acrt.cluster.untyped.backend.ScatterGeometry

class Scatterer(source: PhotonSource, viewLoc: Point, forward: Vect, up: Vect, id: IntersectContainer, width: Int, height: Int, dir: Vect, organizer: ActorRef) extends Actor {
  val interPoint = id.point + id.norm * 1e-8
  val right = forward.cross(up)
  
  organizer ! GeometryOrganizerAll.CastRay(self, scala.util.Random.nextLong(), Ray(interPoint, viewLoc))

  def receive = {
    case PixelHandler.IntersectResult(k, intD) => {
      if(intD.isEmpty) {
        val inRay = (viewLoc - interPoint).normalize
        val scatter = id.geom.asInstanceOf[ScatterGeometry].fractionScattered(dir, inRay, id)
        if (scatter > 0.0) {
          val fracForward = inRay dot forward
          val px = ((inRay.dot(right)/fracForward / 0.707 + 1.0) * width / 2).toInt
          val py = ((-inRay.dot(up)/fracForward / 0.707 + 1.0) * height / 2).toInt
          if (px >= 0 && px < width && py >= 0 && py < height) {
            context.parent ! PhotonCreator.SetColor(px, py, source.light.col * id.color * scatter )
          }
        }
      }
    } 
    case m => "me scatterer. me receive " + m
  }
}
