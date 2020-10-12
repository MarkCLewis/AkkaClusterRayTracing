package acrt.cluster.untyped
import swiftvis2.raytrace.Geometry

trait GeometryCreator {
    def apply(num: String): Geometry
}