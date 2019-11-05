package acrt

import data.InterpolatedCartAndRadSequence
import util.Particle
import swiftvis2.raytrace.Geometry
import swiftvis2.raytrace.RTColor
import swiftvis2.raytrace.GeomSphere
import swiftvis2.raytrace.Point
import ExtendedSlidingBoxSims._
import data.InterpolatedCartAndRadSequence
import swiftvis2.raytrace.KDTreeGeometry
import swiftvis2.raytrace.OffsetGeometry
import swiftvis2.raytrace.Vect
import swiftvis2.raytrace.ListScene

class ExtendedSlidingBoxSims(
  cellSizeX: Double,
  cellSizeY: Double,
  cellCountX: Int,
  cellCountY: Int,
  placedSpecs: Map[(Int, Int), SimSpec],
  backgroundSpecs: Seq[SimSpec],
  shearRate: Double = -1.5 * 2 * math.Pi / 1000, // The -1.5 is from the linearized Hill's solution. The rest is 1000 steps per 2Pi units of time.
  interpCutoff: Double = 1e-5,
  radiusScale: Particle => Double = p => 1.0) {

  private val placedInterps = placedSpecs.mapValues(ss => new InterpolatedCartAndRadSequence(ss.dir, ss.startIndex, ss.endIndex, interpCutoff))
  private val backgroundInterps = backgroundSpecs.map(ss => new InterpolatedCartAndRadSequence(ss.dir, ss.startIndex, ss.endIndex, interpCutoff))
  private val cells = for {
    cx <- -cellCountX to cellCountX
    cy <- -cellCountY to cellCountY
  } yield {
    val interp = placedInterps.get(cx -> cy).getOrElse(backgroundInterps(scala.util.Random.nextInt(backgroundInterps.length)))
    CellData(cx * cellSizeX, cy * cellSizeY, interp)
  }
  val minY = -cellCountY * cellSizeY - cellSizeY * 0.5
  val maxY = cellCountY * cellSizeY + cellSizeY * 0.5
  val totalWidth = maxY - minY

  def geometry(time: Double): Geometry = {
    val interpGeomMap = collection.mutable.Map[InterpolatedCartAndRadSequence, Geometry]()
    val cellGeometry = cells.map { cell =>
      val noOffsetGeom = interpGeomMap.get(cell.simSeq).getOrElse(new KDTreeGeometry(cell.simSeq.particlesAtTime(time).map(geometryForParticle)))
      interpGeomMap(cell.simSeq) = noOffsetGeom
      val unmoddedYOffset = cell.offsetY + shearRate * cell.offsetX * time - minY
      OffsetGeometry(noOffsetGeom, Vect(cell.offsetX, minY + unmoddedYOffset % totalWidth, 0.0))
    }
    new KDTreeGeometry(cellGeometry)
  }

  def geometryForParticle(p: Particle): Geometry = {
    GeomSphere(Point(p.x, p.y, p.z), p.rad * radiusScale(p), p => RTColor.White, p => 0)
  }

}

object ExtendedSlidingBoxSims {
  case class SimSpec(dir: java.io.File, startIndex: Int, endIndex: Int)

  private case class CellData(offsetX: Double, offsetY: Double, simSeq: InterpolatedCartAndRadSequence)
}