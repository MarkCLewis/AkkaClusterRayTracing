package acrt.cluster.untyped

import scala.concurrent.duration._
import akka.util.Timeout
import scala.util.Failure
import scala.util.Success
import akka.actor._
//#frontend
class Frontend extends Actor {
  import Worker._
  var backends = IndexedSeq.empty[ActorRef]
  var jobCounter = 0

  def receive = {
    case job: TransformationJob if backends.isEmpty =>
      sender ! JobFailed("Service unavailable, try again later", job)

    case job: TransformationJob =>
      jobCounter += 1
      backends(jobCounter % backends.size).forward(job)
    
    case TransformationResult(txt) =>
      println(txt)

    case BackendRegistration if !backends.contains(sender) =>
      context.watch(sender)
      backends = backends :+ sender
      println("registered")

    case Terminated(a) =>
      backends = backends.filterNot(_ == a)

    case CastRay(rec, k, r) =>
      rec ! "randtext"
  }
}
//#frontend