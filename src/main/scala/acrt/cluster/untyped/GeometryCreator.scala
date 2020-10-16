package acrt.cluster.untyped
import swiftvis2.raytrace._
import java.net.URL
import data.CartAndRad

sealed trait GeometryCreator extends Serializable {
    def apply(num: String): Geometry
}

class WebCreator(numManagers: Int) extends GeometryCreator {
    def apply(num: String): Geometry = {
      val carURL = new URL("http://www.cs.trinity.edu/~mlewis/Rings/AMNS-Moonlets/Moonlet4/CartAndRad.6029.bin")
      val simpleGeom = CartAndRad.readStream(carURL.openStream).map(p => GeomSphere(Point(p.x, p.y, p.z), p.rad, _ => new RTColor(1, 1, 1, 1), _ => 0.0))
      val ymin = simpleGeom.minBy(_.boundingSphere.center.y).boundingSphere.center.y
      val ymax = simpleGeom.maxBy(_.boundingSphere.center.y).boundingSphere.center.y

      val geomSeqs = simpleGeom.groupBy(g => ((g.boundingSphere.center.y - ymin) / (ymax-ymin) * numManagers).toInt min (numManagers - 1))
      val geoms = geomSeqs.mapValues(gs => new KDTreeGeometry(gs, builder = SphereBoundsBuilder))
      
      val geom = geoms(num.toInt)
      geom
    }
}

class TooMuchRAMCreator(numManagers: Int) extends GeometryCreator {
    def apply(num: String): Geometry = {
      ???
    }
}