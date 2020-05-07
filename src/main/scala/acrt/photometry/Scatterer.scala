package acrt.photometry

import akka.actor.Actor
import swiftvis2.raytrace.{Ray, Vect, IntersectData}
import acrt.geometrymanagement.{GeometryOrganizerAll, GeometryOrganizerFew, GeometryOrganizerSome}

//WIP

class Scatterer(nTimes: Int, nRays: Int) extends Actor {
    import Scatterer._
    //x number of photons into the geom
    def receive = {
        case Scatter(n, intD) => {
            if(n < nTimes) {
                intD match {
                    case None => context.parent ! ??? //send the parent the none
                    case Some(id) => {
                        for(r <- 0 to nRays) {
                            val ray = new Ray(id.point, 
                                new Vect(scala.util.Random.nextDouble(), scala.util.Random.nextDouble(), scala.util.Random.nextDouble())) //Probability distrobution, not random
                            context.parent ! GeometryOrganizerAll.CastRay(self, 0, ray) //send the organizer the ray
                        }
                    } 
                }
            } else {
                context.parent ! ??? //send parent the intersection or not
            }
        }
    }
}
object Scatterer {
    case class Scatter(n: Int, intD: Option[IntersectData])
}