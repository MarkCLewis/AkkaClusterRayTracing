package acrt.photometry.untyped

import akka.actor.Actor
import swiftvis2.raytrace.Geometry
import swiftvis2.raytrace.Point
import swiftvis2.raytrace.Vect
import swiftvis2.raytrace.RTImage
import swiftvis2.raytrace.Ray
import swiftvis2.raytrace.RTColor
import acrt.raytracing.untyped.PixelHandler
import acrt.geometrymanagement.untyped.GeometryOrganizerAll
import akka.actor.Props

class PhotonCreator(xmin: Double, xmax: Double, ymin: Double, ymax: Double, source: PhotonSource, viewLoc: Point, forward: Vect, up: Vect, image: RTImage) extends Actor {
    import PhotonCreator._
    
    val right = forward.cross(up)
    var pixels = Array.fill(image.width, image.height)(RTColor.Black)
    val organizer = Main.organizer
    val rays = collection.mutable.Map[Long, Ray]() 
    private var returnedRays = 0L

    def receive = {
        case SetColor(x, y, col) => {
            context.parent ! ImageDrawer.UpdateColor(x, y, pixels(x)(y) + col)
            pixels(x)(y) = pixels(x)(y) + col
        }
        case PixelHandler.IntersectResult(k, oid) => {
            oid.foreach { iData =>
                val newScatterer = context.actorOf(Props(new Scatterer(source, viewLoc, forward, up, iData, image.width, image.height, rays(k).dir)), s"Scatterer$k")
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
    } 
}
object PhotonCreator {
    case object Render
    case class SetColor(x: Int, y: Int, col: RTColor)
}
