package acrt

import collection.mutable
import swiftvis2.raytrace._

sealed trait IDCombiner extends (Option[IntersectData] => Boolean)
/*
class MergeColors(numRays: Int) extends IDCombiner {
    val buff = mutable.ArrayBuffer[IntersectData]()
    def apply(intD:IntersectData): Boolean = {
        buff += intD
        buff.length >= numRays
    }
    def mergedColor: RTColor = intD.color * (lightColors.foldLeft(new RTColor(0, 0, 0, 1))(_ + _))
}*/
class MergeLightSource(lights: Seq[PointLight], id: IntersectData) extends IDCombiner {
  /* mergeLightSource takes all the lights found by sending out the first ray and 
   * combines them to determine other rays to send out and the color to return */
  val buff = mutable.ArrayBuffer[Option[IntersectData]]()
  def apply(intD: Option[IntersectData]): Boolean = {
    buff += intD
    buff.length >= lights.length
  }
  def merge(): RTColor = {
    val lightColors = for (light <- lights) yield {
        for (intD <- buff) {
          intD match {
            case None => {
              val intensity = (outRay.dir.normalize dot intD.norm).toFloat
              if (intensity < 0) new RTColor(0, 0, 0, 1) else light.col * intensity;
            }
            case Some(nid) => {
              if (nid.time < 0 || nid.time > 1) {
                val intensity = (outRay.dir.normalize dot intD.norm).toFloat
                if (intensity < 0) new RTColor(0, 0, 0, 1) else light.col * intensity;
              } else {
                new RTColor(0, 0, 0, 1)
              }
            }
          }
        }
      }
    lightColors.foldLeft(new RTColor(0, 0, 0, 1))(_ + _)
  }
}