package acrt.photometry.untyped

import swiftvis2.raytrace._

class ScatterSphereGeom(
    center: Point,
    radius: Double,
    color: Point => RTColor,
    reflect: Point => Double
) extends GeomSphere(center, radius, color, reflect)
    with ScatterGeometry {

  def fractionScattered(
      incomingDir: Vect,
      outgoingDir: Vect,
      intersectData: IntersectData
  ): Double = {
    outgoingDir.normalize.dot(intersectData.norm)
  }
}
