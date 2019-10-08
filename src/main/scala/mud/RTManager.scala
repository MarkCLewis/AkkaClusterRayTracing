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

class RTManager(geom: Geometry, lights: List[Light]) extends Actor{
  import RTManager._
  implicit val timeout = Timeout(100.seconds)
  implicit val ec = context.dispatcher
  val router = context.actorOf(BalancingPool(8).props(Props(new RTActor(geom, lights))), "RTRouter")

  def receive = {
    //Consider possibility of CastRays
    case CastRay(i, j, ray) => {
      // println(s"Manager ray $i $j")
      router ! RTActor.CastRay(i, j, ray, sender)
    }
    //TODO: need to rewrite render functions in SwiftVis RayTrace to use actors for parallelization
    //TODO: further actorize, with router
  }
}

object RTManager{
  case class CastRay(i: Int, j: Int, ray: Ray)
}