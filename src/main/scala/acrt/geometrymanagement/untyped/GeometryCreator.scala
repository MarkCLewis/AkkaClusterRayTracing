package acrt.geometrymanagement.untyped

import java.net.URL
import swiftvis2.raytrace.Geometry
import data.CartAndRad
import swiftvis2.raytrace.Point
import swiftvis2.raytrace.GeomSphere
import swiftvis2.raytrace.RTColor
import swiftvis2.raytrace.KDTreeGeometry

sealed trait GeometryCreator {
  def apply(num: String, xyOffset: (Double, Double)): Geometry
}

class WebCreator extends GeometryCreator {
  def apply(num: String, xyOffset: (Double, Double)): Geometry = {
    val carURL = new URL(s"http://www.cs.trinity.edu/~mlewis/Rings/AMNS-Moonlets/Moonlet4/CartAndRad.$num.bin")

    val simpleGeom = CartAndRad.readStream(carURL.openStream).map(p => 
      GeomSphere(Point(p.x + xyOffset._1, p.y + xyOffset._2, p.z), p.rad, _ => new RTColor(1, 1, 1, 1), _ => 0.0))
    val particles = simpleGeom.length
    println(s"Particles#$num: $particles")
    val geom = new KDTreeGeometry(simpleGeom)
    geom
  }
}

class PhotometryCreator extends GeometryCreator {
  def apply(num: String, xyOffset: (Double, Double)): Geometry = {
    val carURL = new URL(s"http://www.cs.trinity.edu/~mlewis/Rings/AMNS-Moonlets/Moonlet4/CartAndRad.$num.bin")

    val simpleGeom = CartAndRad.readStream(carURL.openStream).map(p => 
      new ScatterSphereGeom(Point(p.x + xyOffset._1, p.y + xyOffset._2, p.z), p.rad, _ => new RTColor(1, 1, 1, 1), _ => 0.0))
    val particles = simpleGeom.length
    println(s"Particles#$num: $particles")
    val geom = new KDTreeGeometry(simpleGeom)
    geom
  }
}