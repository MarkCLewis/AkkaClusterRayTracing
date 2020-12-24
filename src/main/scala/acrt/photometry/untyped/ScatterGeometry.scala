package acrt.photometry.untyped

import swiftvis2.raytrace._

trait ScatterGeometry extends Geometry {
  def fractionScattered(incomingDir: Vect, outgoingDir: Vect, intersectData: IntersectData): Double
}
