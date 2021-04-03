package acrt.cluster.untyped.backend.containers

import com.fasterxml.jackson.annotation.{JsonTypeInfo, JsonSubTypes}
import scala.concurrent.{Future, Await, ExecutionContext}
import swiftvis2.raytrace.{Point, RTColor, Geometry, Sphere, Vect, IntersectData, Ray, Box, BoundingBox}
import acrt.cluster.untyped.backend.CborSerializable

//Serializable Container for GeomSpheres, taken mostly from Swiftvis2
case class GeomSphereContainer(center: Point, radius: Double, color: RTColor, reflect: Double) extends Geometry with Sphere with CborSerializable {
    def movedBy(v: Vect): Sphere = copy(center = center+v)
    
    override def intersect(r: Ray): Option[IntersectData] = {
        intersectParam(r).flatMap { case (enter, _, exit, _) =>
        val inter = if (enter < 0) exit else enter
        if (inter < 0) None
        else {
            val pnt = r point inter
            val normal = (pnt - center).normalize
            Some(new IntersectData(inter, pnt, normal, color, reflect, this))
        }
      }
    }
    override def boundingSphere: Sphere = this
    override def boundingBox: Box = BoundingBox(center - radius, center + radius)
}