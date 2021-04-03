package acrt.photometry.untyped

import akka.actor.Actor
import swiftvis2.raytrace.Ray
import swiftvis2.raytrace.Point
import swiftvis2.raytrace.Vect
import swiftvis2.raytrace.RTImage
import swiftvis2.raytrace.RTColor
import swiftvis2.raytrace.IntersectData
import acrt.geometrymanagement.untyped.GeometryOrganizer
import acrt.geometrymanagement.untyped.ScatterGeometry
import acrt.raytracing.untyped.PixelHandler

class Scatterer(source: PhotonSource, viewLoc: Point, forward: Vect, up: Vect, id: IntersectData, width: Int, height: Int, dir: Vect) extends Actor {
  val interPoint = id.point + id.norm * 1e-8
  val right = forward.cross(up)

  Main.organizer ! GeometryOrganizer.CastRay(self, scala.util.Random.nextLong(), Ray(interPoint, viewLoc))
  
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
  }
}