package geo.animation

import geo.core.Feature
import geo.core.Point
import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2

class StaggerVerificationTest {

    @Test
    fun staggerByIndexReturnsCorrectDelays() {
        val features = listOf(
            Feature(Point(0.0, 0.0)),
            Feature(Point(1.0, 0.0)),
            Feature(Point(2.0, 0.0))
        )

        val staggered = features.asSequence().staggerByIndex(50L).toList()

        assertEquals("First feature should have 0ms delay", 0L, staggered[0].delay)
        assertEquals("Second feature should have 50ms delay", 50L, staggered[1].delay)
        assertEquals("Third feature should have 100ms delay", 100L, staggered[2].delay)
    }

    @Test
    fun staggerByDistanceReturnsDelaysProportionalToDistance() {
        val features = listOf(
            Feature(Point(0.0, 0.0)),
            Feature(Point(10.0, 0.0)),
            Feature(Point(20.0, 0.0))
        )

        val origin = Vector2(0.0, 0.0)
        val staggered = features.asSequence().staggerByDistance(origin, 10.0).toList()

        assertEquals("First feature should have 0ms delay", 0L, staggered[0].delay)
        assertEquals("Second feature should have 100ms delay", 100L, staggered[1].delay)
        assertEquals("Third feature should have 200ms delay", 200L, staggered[2].delay)
    }
}
