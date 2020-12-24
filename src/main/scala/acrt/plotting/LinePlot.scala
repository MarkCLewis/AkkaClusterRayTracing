package acrt.plotting

import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.styles._
import swiftvis2.plotting.{Axis, BlackARGB, BlueARGB, Bounds, CategoryAxis, ColorGradient, CyanARGB, Ellipse, GreenARGB, MagentaARGB, NoSymbol, NumericAxis, Plot, Plot2D, PlotDoubleSeries, PlotGrid, PlotIntSeries, PlotLegend, PlotSymbol, PlotText, Rectangle, RedARGB, WhiteARGB, YellowARGB}
import swiftvis2.plotting.styles.ScatterStyle.LineData
import swiftvis2.plotting.Plot.{GridData, TextData, barPlotMap, stacked}
import swiftvis2.plotting.styles.BarStyle.DataAndColor
import swiftvis2.plotting.styles.ScatterStyle
import swiftvis2.plotting.renderer.SwingRenderer
import _root_.swiftvis2.plotting.LegendItem
import swiftvis2.plotting.Illustration
import swiftvis2.plotting.RectangleLine

object LinePlot extends App {

  val all1 = (71.5469 + 69.8576 + 76.0331 + 71.7967 + 68.9194) / 5
  val all2 = (51.4142 + 44.683 + 43.9638 + 53.0459 + 41.9007) / 5
  val all4 = (61.534 + 47.2307 + 51.1508 + 49.0958 + 54.7252) / 5
  val all8 = (83.0114 + 88.9394 + 107.3469 + 75.8766 + 90.3474) / 5
  val all16 = (100.0938 + 114.3831 + 112.1432 + 115.4207 + 124.2039) / 5
  val all32 = (201.9143 + 188.8941 + 173.2371 + 256.3708 + 235.9903) / 5
  val all60 = (310.6468 + 347.2153 + 306.437 + 344.08 + 369.0326) / 5
  
  val some1 = (78.2599 + 82.0255 + 70.4103 + 66.079 + 78.0041) / 5
  val some2 = (43.5675 + 44.0291 + 41.5937 + 53.3171 + 50.0541) / 5
  val some4 = (45.2851 + 51.6777 + 50.8594 + 71.7139 + 49.7869) / 5
  val some8 = (111.3203 + 93.716 + 97.824 + 113.2492 + 108.7542) / 5
  val some16 = (91.4552 + 89.5826 + 91.5343 + 90.1121 + 84.7459) / 5
  val some32 = (53.5019 + 53.6095 + 52.9251 + 52.5884 + 51.0028) / 5
  val some60 = (50.9624 + 49.0895 + 48.6704 + 48.4691 + 48.2514) / 5
  
  val few1 = (64.2039 + 64.8039 + 65.1499 + 61.3789 + 59.7156) / 5
  val few2 = (39.5973 + 41.6009 + 40.646 + 38.5315 + 43.7417) / 5
  val few4 = (54.8816 + 58.1622 + 54.4123 + 50.7824 + 49.523) / 5
  val few8 = (87.0963 + 89.5142 + 80.6101 + 95.3873 + 85.2178) / 5
  val few16 = (71.8881 + 69.0149 + 67.9248 + 72.3536 + 71.0376) / 5
  val few32 = (50.5096 + 45.8682 + 47.5129 + 46.9096 + 49.9311) / 5
  val few60 = (45.2945 + 53.6794 + 47.3112 + 44.3672 + 52.5097) / 5
  
  val all = Seq(all1, all2, all4, all8, all16, all32, all60)
  val some = Seq(some1, some2, some4, some8, some16, some32, some60)
  val few = Seq(few1, few2, few4, few8, few16, few32, few60)
  val numFiles = Seq(1, 2, 4, 8, 16, 32, 60)
  
  val data = Seq(all -> RedARGB, some -> BlueARGB, few -> GreenARGB)

  def scatterLines(): Plot = {

    //Plot.scatterPlotWithLines(xPnt, yPnt, title = "Quadratic", xLabel = xLabel, yLabel = yLabel, lineGrouping = 1)
    Plot.stacked(data.map(t => ScatterStyle(numFiles, t._1, lines = Some(LineData(0, Renderer.StrokeData(1))), colors = t._2)), xLabel = "Num Files", yLabel = "Seconds")
      .withLegend("Legend", PlotLegend(
        Seq(
          LegendItem("All", 
            Seq(Illustration(RedARGB, Some(Rectangle), 10, 10))),
          LegendItem("Some", 
            Seq(Illustration(BlueARGB, Some(Rectangle), 10, 10))),
          LegendItem("Few", 
            Seq(Illustration(GreenARGB, Some(Rectangle), 10, 10)))
        )
      ), Bounds(0.8, 0.2, 0.15, 0.15))
  }

  SwingRenderer(scatterLines(), 1000, 1000, true)
}
