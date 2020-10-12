package acrt.cluster.untyped
import swiftvis2.raytrace._
import java.net.URL
import data.CartAndRad

//sealed trait GeometryCreator extends CborSerializable {
//    def apply(num: String): Geometry
//}

class GeometryCreator {
    def apply(num: String): Geometry = {
      val carURL = new URL("http://www.cs.trinity.edu/~mlewis/Rings/AMNS-Moonlets/Moonlet4/CartAndRad.6029.bin")
      val particles = CartAndRad.readStream(carURL.openStream).map(p => GeomSphere(Point(p.x, p.y, p.z), p.rad, _ => new RTColor(1, 1, 1, 1), _ => 0.0))
      val geom = new KDTreeGeometry(particles)
      geom
    }
}