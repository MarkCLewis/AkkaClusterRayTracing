package acrt.cluster.untyped

import com.fasterxml.jackson.module.scala.JacksonModule
import com.fasterxml.jackson.databind.Module.SetupContext
import swiftvis2.raytrace._

class GeomModule extends JacksonModule {
  override def getModuleName(): String = "geometry"

  this += ((ctx: SetupContext) => {

    ctx.registerSubtypes(
            classOf[GeomSphere], classOf[GeomCylinder], classOf[GeomEllipsoid], 
            classOf[GeomPolyFunc], classOf[GeomPolygon], classOf[GeomBox]
        )
  })
}
