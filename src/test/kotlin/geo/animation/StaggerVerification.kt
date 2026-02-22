package test

import geo.animation.*
import geo.Feature
import geo.Point
import org.openrndr.math.Vector2

fun testStaggerByIndex() {
    val features = listOf(
        Feature(Point(0.0, 0.0)),
        Feature(Point(1.0, 0.0)),
        Feature(Point(2.0, 0.0))
    )
    
    val staggered = features.asSequence().staggerByIndex(50L).toList()
    
    // Verify delays: 0ms, 50ms, 100ms
    check(staggered[0].delay == 0L) { "First feature should have 0ms delay" }
    check(staggered[1].delay == 50L) { "Second feature should have 50ms delay" }
    check(staggered[2].delay == 100L) { "Third feature should have 100ms delay" }
    
    println("✓ staggerByIndex: delays are 0ms, 50ms, 100ms")
}

fun testStaggerByDistance() {
    val features = listOf(
        Feature(Point(0.0, 0.0)),   // distance 0
        Feature(Point(10.0, 0.0)),  // distance 10
        Feature(Point(20.0, 0.0))   // distance 20
    )
    
    val origin = Vector2(0.0, 0.0)
    val staggered = features.asSequence().staggerByDistance(origin, 10.0).toList()
    
    // Verify delays: 0ms, 100ms, 200ms (10ms per unit distance)
    check(staggered[0].delay == 0L) { "First feature should have 0ms delay" }
    check(staggered[1].delay == 100L) { "Second feature should have 100ms delay" }
    check(staggered[2].delay == 200L) { "Third feature should have 200ms delay" }
    
    println("✓ staggerByDistance: delays are 0ms, 100ms, 200ms (proportional to distance)")
}

fun main() {
    testStaggerByIndex()
    testStaggerByDistance()
    println("\n✓ All stagger verification tests passed!")
}
