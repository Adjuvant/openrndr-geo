import geo.LineString
import geo.MultiLineString
import geo.MultiPoint
import geo.Point
import geo.render.Shape
import geo.render.Style
import geo.render.drawMultiLineString
import geo.render.drawMultiPoint
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.draw.loadFont
import org.openrndr.draw.loadImage
import org.openrndr.extra.color.colormatrix.tint
import org.openrndr.math.Vector2
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    configure {
        width = 768
        height = 576
    }

    program {
        val image = loadImage("data/images/pm5544.png")
        val font = loadFont("data/fonts/default.otf", 64.0)

        val multiPoint = MultiPoint(
            listOf(
                Point(0.0, 0.0),
                Point(100.0, 50.0),
                Point(200.0, 100.0)
            )
        )

        val lines = listOf(
            LineString(listOf(Vector2(0.0, 0.0), Vector2(100.0, 50.0))),
            LineString(listOf(Vector2(100.0, 50.0), Vector2(200.0, 100.0)))
        )
        val multiLineString = MultiLineString(lines)

        extend {
            drawer.drawStyle.colorMatrix = tint(ColorRGBa.WHITE.shade(0.2))
            drawer.image(image)

            drawer.fill = ColorRGBa.PINK
            drawer.circle(
                cos(seconds) * width / 2.0 + width / 2.0,
                sin(0.5 * seconds) * height / 2.0 + height / 2.0,
                140.0
            )

            // Draw all line strings with same style
//            drawMultiLineString(drawer, multiLineString, projection, Style {
//                stroke = ColorRGBa.BLUE
//                strokeWeight = 2.0
//                lineCap = LineCap.ROUND
//            })

            // Draw all points with same style
//            drawMultiPoint(drawer, multiPoint, projection, Style {
//                size = 8.0
//                shape = Shape.Square
//                stroke = ColorRGBa.BLACK
//                fill = ColorRGBa.RED
//            })

            drawer.fontMap = font
            drawer.fill = ColorRGBa.WHITE
            drawer.text("OPENRNDR", width / 2.0, height / 2.0)
        }
    }
}
