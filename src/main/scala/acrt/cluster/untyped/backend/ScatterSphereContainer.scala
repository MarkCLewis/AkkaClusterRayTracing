package acrt.cluster.untyped.backend
import swiftvis2.raytrace._

class ScatterSphereContainer(
    center: Point,
    radius: Double,
    color: RTColor,
    reflect: Double
) extends GeomSphereContainer(center, radius, color, reflect)
    with ScatterGeometry {

  def fractionScattered(
      incomingDir: Vect,
      outgoingDir: Vect,
      intersectData: IntersectData
  ): Double = {
    outgoingDir.normalize.dot(intersectData.norm)
  }
}
