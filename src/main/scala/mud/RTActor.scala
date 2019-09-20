package mud

import akka.actor.Actor
import data.CartAndRad
import swiftvis2.raytrace._
import swiftvis2.raytrace.LinearViewPath._

class RTActor extends Actor {
  import RTActor._
  def recieve = {
    case Subset(subst) => {
      //TODO: trace subset
      RayTrace.render(???)
    }
    case _ =>
  }
}
object RTActor {
  case class Subset(subst: KDTreeGeometry)
}