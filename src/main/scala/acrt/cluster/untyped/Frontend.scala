package acrt.cluster.untyped

import scala.concurrent.duration._
import akka.util.Timeout
import scala.util.Failure
import scala.util.Success
import akka.actor._
import swiftvis2.raytrace._
//#frontend
class Frontend(img: rendersim.RTBufferedImage, numRays: Int, lights: List[PointLight]) extends Actor {
  import GeometryManager._
  import GeometryOrganizerAll._
  import Frontend._
  private var backends = IndexedSeq.empty[ActorRef]
  private var jobCounter = 0

  val organizer = context.actorOf(Props(new GeometryOrganizerAll()), "GeometryOrganizer")
  val imageDrawer = context.actorOf(Props(new ImageDrawer(lights, img, numRays, organizer)), "ImageDrawer")

  val cellWidth = 1e-5
  val distanceUp = 1e-5
  val viewSize = 1e-5
  val numSims = 6
  val firstXOffset = cellWidth * (numSims - 1)
      
  val eye = Point(0, 0, distanceUp)
  val topLeft = Point(-viewSize, viewSize, distanceUp - viewSize)
  val right = Vect(2 * viewSize, 0, 0)
  val down = Vect(0, -2 * viewSize, 0)

  def receive = {
    case Start =>
      imageDrawer ! ImageDrawer.Start(eye, topLeft, right, down)

    case job: TransformationJob if backends.isEmpty =>
      sender ! JobFailed("Service unavailable, try again later", job)

    case job: TransformationJob =>
      jobCounter += 1
      backends(jobCounter % backends.size).forward(job)
    
    case TransformationResult(txt) =>
      println(txt)

    case BackendRegistration =>
     if (!backends.contains(sender)) {
       context.watch(sender)
       backends = backends :+ sender
       organizer ! ManagerRegistration(sender)
     }

    case Terminated(a) =>
      backends = backends.filterNot(_ == a)
  }
}
//#frontend

object Frontend {
  case object Start extends Serializable
}