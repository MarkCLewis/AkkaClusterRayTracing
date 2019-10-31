package mud

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
import Combiners._

class ImageDrawer(geom: Geometry, lights: List[Light], img: rendersim.RTBufferedImage, numRays: Int) extends Actor {
  import ImageDrawer._

  val aspect = img.width.toDouble / img.height
  implicit val timeout = Timeout(100.seconds)
  implicit val ec = context.dispatcher
  val manager:ActorRef = context.actorOf(Props(new RTManager(geom, lights)), "RTMan")
  var ids: Map[Long, IntersectData => Boolean] = Nil
  val mc:IDCombiner = new MergeColors(1)
  val ml:IDCombiner = new MergeLightSource(1)

  def receive = {
    case Start(eye, topLeft, right, down) =>  
      for (i <- (0 until img.width); j <- (0 until img.height)) {
        /*(0 until numRays).map(index => {
            manager ! RTManager.CastRay(i, j, 
              Ray(eye, topLeft + right * (aspect * (i + (if (index > 0) math.random * 0.75 else 0)) / img.width) + down * (j + (if (index > 0) math.random * 0.75 else 0)) / img.height)
            )}
            )*/
          val r = Ray(eye, topLeft + right * (aspect * (i + (if (index > 0) math.random * 0.75 else 0)) / img.width) + down * (j + (if (index > 0) math.random * 0.75 else 0)) / img.height)
          val intsct = geom intersect r
          intsct match {
            case None => self ! setColor(i,j,RTColor(0.0, 0.0, 0.0, 1.0))
            case Some(intD) => {
              val geomSize = intD.geom.boundingSphere.radius
              val id = scala.util.Random.nextLong()
              ids = ids ++ (id, ml.merge())
            }
          }


      }
    case SetColor(i, j, color) =>
            println(s"Setting $i, $j")
            img.setColor(i, j, color)
    case m => println("Unhandled message "+m)
  }
}

object ImageDrawer{
  case class Start(eye: Point, topLeft: Point, right: Vect, down: Vect)
  case class SetColor(i: Int, j: Int, color: RTColor)
}