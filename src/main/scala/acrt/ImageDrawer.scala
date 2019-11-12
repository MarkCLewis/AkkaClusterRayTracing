package acrt

import akka.actor.Actor
import akka.pattern.ask
import data.CartAndRad
import swiftvis2.raytrace._
import swiftvis2.raytrace.LinearViewPath._
import akka.routing.BalancingPool
import akka.actor.Props
import scala.concurrent.duration._
import akka.util.Timeout
import akka.actor.ActorRef
import playground.RTBufferedImage

class ImageDrawer(geom: Geometry, lights: List[PointLight], img: rendersim.RTBufferedImage, numRays: Int) extends Actor {
  import ImageDrawer._

  val aspect = img.width.toDouble / img.height
  implicit val timeout = Timeout(100.seconds)
  implicit val ec = context.dispatcher
  
  val ids = collection.mutable.Map[Long, ActorRef]()
  //val mc = new MergeColors(1)

  def receive = {
    case Start(eye, topLeft, right, down) => {
      for (i <- (0 until img.width); j <- (0 until img.height)) {
        val pix = context.actorOf(Props(new PixelHandler(lights, i, j)), ""+i+j)
        val index = 0
        val r = Ray(eye, topLeft + right * (aspect * (i + (if (index > 0) math.random * 0.75 else 0)) / img.width) + down * (j + (if (index > 0) math.random * 0.75 else 0)) / img.height)
        pix ! PixelHandler.AddRay(r)
      }
    }
    /*case IntersectResult(key, intD) => {
      if (ids(key)(intD)) {
        intD match {
          case None => //self ! SetColor(i,j, new RTColor(0.0, 0.0, 0.0, 1.0))
          case Some(id) => {
            val geomSize = id.geom.boundingSphere.radius
            val rt = ids(key) match {
              case ml: MergeLightSource =>
              val merged = ml.merge(id, geom)
            }
            //self ! SetColor(self, key, r)
          }
        }
        ids.remove(key)
      }
    }*/
    case SetColor(i, j, color) =>
      println(s"Setting $i, $j")
      img.setColor(i, j, color)
    case m => "me imagedrawer. me recieve " + m
  }
}

object ImageDrawer {
  case class Start(eye: Point, topLeft: Point, right: Vect, down: Vect)
  case class SetColor(i: Int, j: Int, color: RTColor)
}