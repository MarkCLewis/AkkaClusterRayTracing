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

class ImageDrawer(geom: Geometry, lights: List[Light], img: rendersim.RTBufferedImage, numRays: Int) extends Actor{
  import ImageDrawer._

  implicit val timeout = Timeout(100.seconds)
  implicit val ec = context.dispatcher
  val manager:ActorRef = context.actorOf(Props(new RTManager(geom, lights)), "RTMan")
  def receive = {
    case CastRay(x,y,ray) => {   
      //val send = sender
      val fut = manager ? RTActor.CastRay(ray)
      fut.foreach({
        case colors:Seq[RTColor] =>  
            println(s"Setting $x, $y")
            img.setColor(x, y, colors.reduceLeft(_ + _) / numRays)
      })
    }
  }
}

object ImageDrawer{
  case class CastRay(x: Int, y: Int ,ray: Ray)
}