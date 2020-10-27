package acrt.cluster.untyped

import swiftvis2.raytrace._
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonSubTypes
import scala.concurrent._

case class IntersectContainer(time: Double, point: Point, norm: Vect, color: RTColor, reflect: Double, 
  /*@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
      @JsonSubTypes(
        Array(
          new JsonSubTypes.Type(value = classOf[GeomSphere], name = "geomsphere"),
          new JsonSubTypes.Type(value = classOf[GeomSphereContainer], name = "geomsphere")))*/
      geom: Geometry)
      
object IntersectContainer {
  def apply(id: IntersectData): IntersectContainer = {
    val ic = new IntersectContainer(id.time, id.point, id.norm, id.color, id.reflect, id.geom)
    ic
  }
}

case class GeomSphereContainer(center: Point, radius: Double, color: RTColor, reflect: Double) extends Geometry with Sphere {
    
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

class KDTreeContainer[B <: Bounds](geometry: Seq[Geometry], val MaxGeom: Int = 5, builder: BoundsBuilder[B] = SphereBoundsBuilder)(implicit ec: ExecutionContext) extends Geometry {
  import KDTreeContainer._

  private val root = buildTree(geometry)
  def intersect(r: Ray): Option[IntersectData] = {
    def helper(n: Node[B]): Option[IntersectData] = n match {
      case InternalNode(g, splitDim, splitValue, left, right, bounds) =>
        //        println(s"leftb = ${left.boundingSphere}, lefti = ${left.boundingSphere.intersectParam(r)}")
        //        println(s"rightb = ${right.boundingSphere}, righti = ${right.boundingSphere.intersectParam(r)}")
        val leftHit = left.bounds.intersectParam(r).filter(_._3 >= 0).map(p => left -> p._1)
        val rightHit = right.bounds.intersectParam(r).filter(_._3 >= 0).map(p => right -> p._1)
        val hits = (leftHit.toList ++ rightHit.toList).sortBy(_._2)
        //        println(hits)
        hits.foldLeft(None: Option[IntersectData]) {
          case (None, (child, param)) => helper(child)
          case (oid @ Some(id), (child, param)) =>
            if (id.time < param) oid // There was a hit in the first child before we cross the boundingSphere of the second child.
            else helper(child) match {
              case None             => oid
              case coid @ Some(cid) => if (id.time < cid.time) oid else coid
            }
        }
      case LeafNode(g, bounds) =>
        val hits = g.flatMap(_.intersect(r))
        if (hits.isEmpty) None else Some(hits.minBy(_.time))
    }
    helper(root)
  }

  override def boundingSphere: Sphere = root.bounds.boundingSphere

  override def boundingBox: Box = root.bounds.boundingBox

  private def buildTree(geom: Seq[Geometry]): Node[B] = {
    def helper(g: Seq[Geometry], min: Point, max: Point, level: Int): Future[Node[B]] = {
      val body = () => {
        val size = (max - min).magnitude
        val (here, children) = g.partition(_.boundingSphere.radius > size)
        val actualMin = g.map(g => g.boundingSphere.center.offsetAll(-g.boundingSphere.radius)).reduceLeft(_ min _)
        val actualMax = g.map(g => g.boundingSphere.center.offsetAll(g.boundingSphere.radius)).reduceLeft(_ max _)
        if (children.isEmpty || g.length <= MaxGeom) {
          val bounds = builder.fromMinMax(actualMin, actualMax)
          Future.successful(LeafNode[B](g, bounds))
        } else {
          val splitDim = (max - min).maxDim
          val (splitValue, before, after) = partitionOnDim(g.toArray, splitDim)
          val leftF = helper(before, min, max.updateDim(splitDim, splitValue), level + 1)
          val rightF = helper(after, min.updateDim(splitDim, splitValue), max, level + 1)
          for (left <- leftF; right <- rightF) yield {
            val bounds = builder.fromMinMax(actualMin, actualMax)
            InternalNode[B](here, splitDim, splitValue, left, right, bounds)
          }
        }
      }
      (if(level < 8) Future(body()) else Future.successful(body())).flatMap(x => x)
    }
    Await.result(helper(geom, geom.map(g => g.boundingSphere.center.offsetAll(-g.boundingSphere.radius)).reduceLeft(_ min _),
      geom.map(g => g.boundingSphere.center.offsetAll(g.boundingSphere.radius)).reduceLeft(_ max _), 0), scala.concurrent.duration.Duration.Inf)
  }

  private def partitionOnDim(g: Array[Geometry], dim: Int): (Double, Array[Geometry], Array[Geometry]) = {
    def helper(start: Int, end: Int): Unit = {
      val pivot = g((start + end) / 2).boundingSphere.center(dim)
      var low = start - 1
      var high = end
      while (low < high) {
        do { low += 1 } while (g(low).boundingSphere.center(dim) < pivot)
        do { high -= 1 } while (g(high).boundingSphere.center(dim) > pivot)
        if (low < high) {
          val tmp = g(low)
          g(low) = g(high)
          g(high) = tmp
        }
      }
      if (high < g.length / 2) helper(high + 1, end)
      else if (high > g.length / 2) helper(start, high)
    }
    helper(0, g.length)
    (g(g.length / 2).boundingSphere.center(dim), g.slice(0, g.length / 2), g.slice(g.length / 2, g.length))
  }
}

object KDTreeContainer {
 sealed trait Node[B] extends Serializable {
    val g: Seq[Geometry]
    val bounds: B
  }
 case class InternalNode[B](g: Seq[Geometry], splitDim: Int, splitValue: Double, left: Node[B], right: Node[B], bounds: B) extends Node[B]
 case class LeafNode[B](g: Seq[Geometry], bounds: B) extends Node[B]
}