package acrt.cluster.untyped.backend.containers

import swiftvis2.raytrace._

trait ScatterGeometry extends Geometry {
  def fractionScattered(incomingDir: Vect, outgoingDir: Vect, intersectData: IntersectContainer): Double
}
