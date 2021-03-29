package acrt.cluster.untyped.frontend

import java.net.URL
import data.CartAndRad
import scala.concurrent.ExecutionContext
import com.fasterxml.jackson.annotation.{JsonTypeInfo, JsonSubTypes}
import swiftvis2.raytrace.{Point, Geometry, RTColor, SphereBoundsBuilder, BoxBoundsBuilder}
import acrt.cluster.untyped.backend.{CborSerializable, GeomSphereContainer, ScatterSphereContainer, KDTreeContainer}

//JSON Tag info for the GeometryCreator trait
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[WebCreator], name = "webcreator"),
    new JsonSubTypes.Type(value = classOf[PhotometryCreator], name = "photometrycreator"),
    new JsonSubTypes.Type(value = classOf[FileCreator], name = "filecreator")))
sealed trait GeometryCreator extends CborSerializable with Serializable {
    def apply(num: String, xyOffset: (Double, Double))(implicit ec: ExecutionContext): Geometry
}

//Creates a Geometry with info downloaded from cs.trinity.edu
class WebCreator extends GeometryCreator {
    def apply(num: String, xyOffset: (Double, Double))(implicit ec: ExecutionContext): Geometry = {
      val carURL = new URL(s"http://www.cs.trinity.edu/~mlewis/Rings/AMNS-Moonlets/Moonlet4/CartAndRad.$num.bin")

      val simpleGeom = CartAndRad.readStream(carURL.openStream).map(p => 
        GeomSphereContainer(Point(p.x + xyOffset._1, p.y + xyOffset._2, p.z), p.rad, new RTColor(1, 1, 1, 1), 0.0))
      val particles = simpleGeom.length
      println(s"Particles#$num: $particles")
      val geom = new KDTreeContainer(simpleGeom, builder = SphereBoundsBuilder)
      geom
    }
}

//Creates a Geometry with info downloaded from cs.trinity.edu
class PhotometryCreator extends GeometryCreator {
    def apply(num: String, xyOffset: (Double, Double))(implicit ec: ExecutionContext): Geometry = {
      val carURL = new URL(s"http://www.cs.trinity.edu/~mlewis/Rings/AMNS-Moonlets/Moonlet4/CartAndRad.$num.bin")

      val simpleGeom = CartAndRad.readStream(carURL.openStream).map(p => 
        new ScatterSphereContainer(Point(p.x + xyOffset._1, p.y + xyOffset._2, p.z), p.rad, new RTColor(1, 1, 1, 1), 0.0))
      val particles = simpleGeom.length

      println(s"Particles#$num: $particles")
      val geom = new KDTreeContainer(simpleGeom, builder = BoxBoundsBuilder)
      geom
    }
}


class FileCreator extends GeometryCreator {
    def apply(num: String, xyOffset: (Double, Double))(implicit ec: ExecutionContext): Geometry = {
      ???
    }
}
