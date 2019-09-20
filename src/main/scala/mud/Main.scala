package mud

import swiftvis2.raytrace._

/**
This is a stub for the main class for your MUD.
*/

import data.CartAndRad
import swiftvis2.raytrace._
import swiftvis2.raytrace.LinearViewPath._
import java.awt.image.BufferedImage
import scala.swing._
import swiftvis2.
import java.io.File
import ExtendedSlidingBoxSims.SimSpec
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.IIOImage

class RTBufferedImage(img: BufferedImage) extends RTImage {
  def width: Int = img.getWidth
  def height: Int = img.getHeight
  def setColor(x: Int, y: Int, color: RTColor): Unit = {
    img.setRGB(x, y, color.toARGB)
  }
}

object Main extends App {
  val saturnImage = javax.imageio.ImageIO.read(new File("/data/mlewis/Rings/AMNS-Moonlets/8k_saturn.jpg"))
  val saturnTexture = SphereTextureColorFunc(saturnImage, Point(-1, 0, 0))
  val saturnGeom = GeomSphere(Point(-1, 0, 0), 0.28, saturnTexture, p => 0)
  // TODO - The brightness of these should go as the dot product of the angle with the incoming sunlight.
  val saturnLights = (for(i <- -60 to 60 by 60; j <- -60 to 60 by 60) yield {
    val theta = i*math.Pi/180
    val phi = j*math.Pi/180
    PointLight(RTColor(0.025, 0.02, 0.015), Point(0.3*math.cos(theta)*math.cos(phi), 0.3*math.sin(theta)*math.cos(phi), 0.3*math.cos(phi)), Set(saturnGeom))
  }).toList
  val backgroundSpecs = Seq(
      SimSpec(new File("/data/mlewis/Rings/AMNS-Moonlets/Equib/"), 10000, 10029),
      SimSpec(new File("/data/mlewis/Rings/AMNS-Moonlets/Equib/"), 11000, 11029),
      SimSpec(new File("/data/mlewis/Rings/AMNS-Moonlets/Moonlet3/"), 13000, 13029),
      SimSpec(new File("/data/mlewis/Rings/AMNS-Moonlets/Moonlet3/"), 14000, 14029),
      SimSpec(new File("/data/mlewis/Rings/AMNS-Moonlets/Moonlet3/"), 15000, 15029))
  val placedSpecs = Map(
      (-1, 0) -> SimSpec(new File("/data/mlewis/Rings/AMNS-Moonlets/Moonlet1/"), 5000, 5029),
      (1, 0) -> SimSpec(new File("/data/mlewis/Rings/AMNS-Moonlets/Moonlet2/"), 10000, 10029),
      (0, 0) -> SimSpec(new File("/data/mlewis/Rings/AMNS-Moonlets/Moonlet4/"), 4000, 4029))
  val esbs = new ExtendedSlidingBoxSims(2e-5, 2e-4, 10, 1, placedSpecs, backgroundSpecs)
  val img = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB)
  val rtImg = new RTBufferedImage(img)
  val path = LinearViewPath(List(
      StopPoint(View(Point(0, 0, 5e-4), Vect(0, 0, -1), Vect(-1, 0, 0)), 2),
      StopPoint(View(Point(7.4e-8, -1.8e-6, 4e-5), Vect(0, 0, -1), Vect(-1, 0, 0)), 10),
      StopPoint(View(Point(7.4e-8, -1.8e-6, 4e-6), Vect(0, 0, -1), Vect(-1, 0, 0)), 10),
      StopPoint(View(Point(2e-6, -1.8e-6, 7e-7), Vect(-1, 0, 0), Vect(0, 0, 1)), 10)), 
      List(10, 5, 5), LinearViewPath.SmoothEasing)
  val frame = new JFrame("Trace Frame")
  val secsPerFrame = 0.1
  val secsPerStep = 1 //45
  val sunElev = 10.0*math.Pi/180
  for((view, i) <- path.atIntervals(secsPerFrame).zipWithIndex) {
    println(view)
    val stepTime = i*secsPerFrame/secsPerStep
    val geometry = new ListScene(saturnGeom, esbs.geometry(stepTime))
    val sunTheta = stepTime*math.Pi*2/1000
    val lights = PointLight(RTColor.White, Point(100*math.cos(sunElev)*math.cos(sunTheta), -100*math.cos(sunElev)*math.sin(sunElev), 100*math.sin(sunElev))) :: saturnLights 
    RayTrace.render(view, rtImg, geometry, lights, 10)
    frame.repaint()
    val istr = i.toString
    ImageIO.write(img, "png", new File(s"/data/mlewis/Rings/AMNS-Moonlets/Frame.${"0"*(4-istr.length)+istr}.png"))
  }
}