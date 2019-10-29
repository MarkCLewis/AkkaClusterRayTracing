package mud
import swiftvis2.raytrace.IntersectData

sealed trait IDCombiner extends (IntersectData => Boolean)

class MergeColors(numRays: Int) extends IDCombiner {
    var buff: mutable.ArrayBuffer[IntersectData] = Nil

    def merge(intD:IntersectData) = {
        if(buff.length >= numRays) {
            intD.color * (lightColors.foldLeft(new RTColor(0, 0, 0, 1))(_ + _))
        }
        else 
        {
            buff = buff++intD
        }
    } 
}
class MergeLightSource(numRays:Int) extends IDCombiner {
    var buff: mutable.ArrayBuffer[IntersectData] = Nil
    def merge(intD:IntersectData) = {
        val lightColors = for (light <- lights) yield light.color(intD, geom)
    }
}