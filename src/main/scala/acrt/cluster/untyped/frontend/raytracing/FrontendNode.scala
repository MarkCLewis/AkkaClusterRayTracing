package acrt.cluster.untyped.frontend.raytracing

import akka.actor.{Actor, ActorRef, Props, Terminated}
import swiftvis2.raytrace.{PointLight, Point, Vect}
import acrt.cluster.untyped.backend.CborSerializable

class FrontendNode(img: rendersim.RTBufferedImage, numRays: Int, lights: List[PointLight]) extends Actor {
  import Frontend._

  private var backends = IndexedSeq.empty[ActorRef]
  private var jobCounter = 0

  //Change to change how many files loaded, or how many backends to look for
  val numFiles = 40
  val numBackend = 8

  //Change to change what style of Organizer
  val organizer = context.actorOf(Props(new GeometryOrganizerFew(numFiles, numBackend)), "GeometryOrganizer")
  val imageDrawer = context.actorOf(Props(new ImageDrawer(lights, img, numRays, organizer)), "ImageDrawer")

  //Automatically changes view based on the number of files
  //val eye = Point(0.0, 0.0, numFiles*1e-5)
  //val topLeft = Point(-1e-5, 1e-5, (numFiles-1)*1e-5)
  //val right = Vect(2 * 1e-5, 0, 0)
  //val down = Vect(0, -2 * 1e-5, 0)

  val n = math.sqrt(numFiles.toDouble / 10.0).ceil.toInt
  val eye = Point(0.0, 0.0, (10 * n)*1e-5)
  val topLeft = Point(-1e-5, 1e-5, ((10 * n)-1)*1e-5)
  val right = Vect(2 * 1e-5, 0, 0)
  val down = Vect(0, -2 * 1e-5, 0)

  def receive = {
    //Forwards the BackendRegistration to the Organizer
    case BackendRegistration => {
      organizer ! GeometryOrganizerAll.BackendRegistration(sender)
    }

    //Start the ImageDrawer
    case Start =>
      imageDrawer ! ImageDrawer.Start(eye, topLeft, right, down)

    //If a backend dies, removes backends from it
    case Terminated(a) =>
      backends = backends.filterNot(_ == a)
  }
}

object Frontend {
  case object Start extends CborSerializable
  case object BackendRegistration extends CborSerializable
}
