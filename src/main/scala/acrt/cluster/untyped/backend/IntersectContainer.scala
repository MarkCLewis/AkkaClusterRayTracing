package acrt.cluster.untyped.backend

import com.fasterxml.jackson.annotation.{JsonTypeInfo, JsonSubTypes}
import swiftvis2.raytrace.{Point, Vect, RTColor, GeomSphere, Sphere, Box, Geometry, IntersectData}

//Serializable Container for IntersectData, with JSON tags for Jackson
case class IntersectContainer(time: Double, point: Point, norm: Vect, color: RTColor, reflect: Double, 
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes(
        Array(
          new JsonSubTypes.Type(value = classOf[GeomSphere], name = "geomsphere"),
          new JsonSubTypes.Type(value = classOf[KDTreeContainer[Sphere]], name = "kdtreecontainersphere"),
          new JsonSubTypes.Type(value = classOf[KDTreeContainer[Box]], name = "kdtreecontainerbox"),
          new JsonSubTypes.Type(value = classOf[GeomSphereContainer], name = "geomspherecontainer")))
      geom: Geometry) extends CborSerializable
      
object IntersectContainer {
    //Apply for easy conversion from IntersectData to IntersectContainer
    def apply(id: IntersectData): IntersectContainer = {
        val ic = new IntersectContainer(id.time, id.point, id.norm, id.color, id.reflect, id.geom)
        ic
    }
}