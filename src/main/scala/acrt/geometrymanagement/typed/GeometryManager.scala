package acrt.geometrymanagement.typed

import akka.actor.typed.{ActorRef, Behavior, Props, SupervisorStrategy}
import akka.actor.typed.scaladsl.{Behaviors, ActorContext, Routers}
import akka.routing.BalancingPool
import swiftvis2.raytrace.{Geometry, Ray}
import acrt.geometrymanagement.typed
import acrt.raytracing.typed.PixelHandler

object GeometryManager {
  case class CastRay(recipient: ActorRef[PixelHandler.IntersectResult], k: Long, ray: Ray, geomOrg: ActorRef[GeometryOrganizer.RecID])

  def apply(geom: Geometry): Behavior[CastRay] = 
  Behaviors.receive { (context, message) =>
    val pool = Routers.pool(poolSize = Runtime.getRuntime().availableProcessors())(
        // make sure the workers are restarted if they fail
        Behaviors.supervise(Intersector(geom)).onFailure[Exception](SupervisorStrategy.restart))
    val router = context.spawn(pool, "IntersectRouter")
    val k = message.k
    context.log.info(s"Casting Ray $k to router.")
    router ! Intersector.CastRay(message.k, message.ray, message.recipient, message.geomOrg)
    Behaviors.same
  }
}