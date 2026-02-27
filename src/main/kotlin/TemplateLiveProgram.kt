import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.math.Vector2
import org.openrndr.shape.shape
import kotlin.math.sin

/**
 *  This is a template for a live program.
 *
 *  It uses oliveProgram {} instead of program {}. All code inside the
 *  oliveProgram {} can be changed while the program is running.
 */

fun main() = application {
    configure {
        width = 800
        height = 800
    }
    oliveProgram {
        extend {
            // Test of shape builder: import org.openrndr.shape.shape
            drawer.stroke = ColorRGBa.BLACK
            val s = shape {
                // Base shape
                contour {
                    moveTo(Vector2(width / 2.0 - (120.0), height / 2.0 - 120.00))
                    lineTo(cursor + Vector2(240.0, 0.0))
                    lineTo(cursor + Vector2(0.0, 240.0))
                    lineTo(anchor)
                    close()
                }
                // hole
                contour {
                    moveTo(Vector2(width / 2.0 - 90.0, height / 2.0 - 100.0))
                    lineTo(cursor + Vector2(120.0, 0.0))
                    lineTo(cursor + Vector2(0.0, 90.00))
                    lineTo(anchor)
                    close()
                }
                // hole
                contour{
                    moveTo(Vector2(width / 2.0 + 50.0, height / 2.0 - 100.0))
                    lineTo(cursor + Vector2(40.0, 0.0))
                    lineTo(cursor + Vector2(0.0, 90.00))
                    close()
                }
            }
            drawer.clear(ColorRGBa.WHITE)
            drawer.fill = ColorRGBa.PINK

            drawer.shape(s)
        }
    }
}