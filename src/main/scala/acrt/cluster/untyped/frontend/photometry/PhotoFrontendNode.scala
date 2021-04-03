package acrt.cluster.untyped.frontend.photometry

import akka.actor.{Actor, ActorRef, Props, Terminated}
import swiftvis2.raytrace.{PointLight, Point, Vect}
import acrt.cluster.untyped.backend.CborSerializable
import acrt.cluster.untyped.frontend.GeometryOrganizer
import acrt.cluster.untyped.frontend.FrontendNode

class PhotoFrontendNode(val img: rendersim.RTBufferedImage, val numRays: Int, lights: List[PhotonSource]) extends FrontendNode {
  import FrontendNode._

  private var backends = IndexedSeq.empty[ActorRef]
  private var jobCounter = 0

  //Change to change how many files loaded, or how many backends to look for
  val numFiles = 40
  val numBackend = 8

  //Change to change what style of Organizer
  val organizer = context.actorOf(Props(new GeometryOrganizerSome(numFiles, numBackend)), "GeometryOrganizer")
  val imageDrawer = context.actorOf(Props(new ImageDrawer(lights, img, numRays, numFiles, organizer)), "ImageDrawer")

  //Automatically changes view based on the number of files
  val eye = Point(0.0, 0.0, numFiles*1e-5)
  val topLeft = Point(-1e-5, 1e-5, (numFiles-1)*1e-5)
  val right = Vect(2 * 1e-5, 0, 0)
  val down = Vect(0, -2 * 1e-5, 0)

  def receive = {
    //Forwards the BackendRegistration to the Organizer
    case BackendRegistration => {
      organizer ! GeometryOrganizer.BackendRegistration(sender)
    }

    //Start the ImageDrawer
    case Start =>
      imageDrawer ! ImageDrawer.AcquireBounds

    //If a backend dies, removes backends from it
    case Terminated(a) =>
      backends = backends.filterNot(_ == a)
  }
}

