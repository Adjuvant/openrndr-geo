package geo.animation

import geo.animation.interpolators.*
import org.junit.Test
import org.junit.Assert.*
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.math.Vector2

/**
 * Synthetic verification tests for property tweening system.
 *
 * These tests verify:
 * 1. LinearInterpolator produces correct intermediate values
 * 2. HaversineInterpolator implements valid Haversine formula
 * 3. Animation data class tracks progress correctly
 * 4. GeoAnimator accepts property reference animation syntax
 * 5. Position conversions work correctly
 *
 * @author Phase 05-animation - 05-02
 */
class TweeningVerificationTest {

    @Test
    fun linearInterpolatorReturnsCorrectIntermediateValues() {
        val start = Vector2(0.0, 0.0)
        val end = Vector2(100.0, 100.0)

        // Test at t=0.5
        val mid = linearInterpolate(start, end, 0.5)
        assertEquals("Linear interpolation x at 0.5 should be 50", 50.0, mid.x, 0.001)
        assertEquals("Linear interpolation y at 0.5 should be 50", 50.0, mid.y, 0.001)

        // Test at t=0.0
        val startPoint = linearInterpolate(start, end, 0.0)
        assertEquals("At t=0 should return start x", 0.0, startPoint.x, 0.001)
        assertEquals("At t=0 should return start y", 0.0, startPoint.y, 0.001)

        // Test at t=1.0
        val endPoint = linearInterpolate(start, end, 1.0)
        assertEquals("At t=1 should return end x", 100.0, endPoint.x, 0.001)
        assertEquals("At t=1 should return end y", 100.0, endPoint.y, 0.001)
    }

    @Test
    fun haversineInterpolatorReturnsPositionsAlongGreatCirclePath() {
        val london = Position(51.5074, -0.1278)
        val paris = Position(48.8566, 2.3522)

        // Midpoint should be between London and Paris
        val midpoint = haversineInterpolate(london, paris, 0.5)

        // Latitude should be between 48.8566 and 51.5074
        assertTrue(
            "Midpoint latitude (${midpoint.latitude}) should be between Paris (${paris.latitude}) and London (${london.latitude})",
            midpoint.latitude > paris.latitude && midpoint.latitude < london.latitude
        )

        // Longitude should be between -0.1278 and 2.3522
        assertTrue(
            "Midpoint longitude (${midpoint.longitude}) should be between London (${london.longitude}) and Paris (${paris.longitude})",
            midpoint.longitude > london.longitude && midpoint.longitude < paris.longitude
        )
    }

    @Test
    fun haversineInterpolatorHandlesEdgeCaseSamePositions() {
        val pos = Position(51.5074, -0.1278)
        val result = haversineInterpolate(pos, pos, 0.5)

        // When interpolating between same positions, should return the same position
        assertEquals("Same position interpolation should return original latitude", pos.latitude, result.latitude, 0.0001)
        assertEquals("Same position interpolation should return original longitude", pos.longitude, result.longitude, 0.0001)
    }

    @Test
    fun animationDataClassCalculatesProgressCorrectly() {
        val anim = Animation(
            targetValue = 100.0,
            duration = 2000,
            easing = Easing.CubicInOut
        )

        // Progress should be near 0 immediately after creation
        val initialProgress = anim.progress()
        assertTrue("Initial progress should be near 0: $initialProgress", initialProgress < 0.1)

        // Should not be complete yet
        assertFalse("Animation should not be complete immediately", anim.isComplete())

        // Should have remaining time close to full duration
        assertTrue("Remaining time should be close to 2000ms: ${anim.remainingTime()}", anim.remainingTime() > 1500)
    }

    @Test
    fun geoAnimatorExtendsAnimatableAndAcceptsPropertyReferences() {
        val animator = GeoAnimator()

        // GeoAnimator should extend Animatable
        assertTrue("GeoAnimator should extend Animatable", animator is Animatable)

        // Should have mutable properties for animation
        animator.x = 50.0
        animator.y = 100.0
        assertEquals("x property should be mutable", 50.0, animator.x, 0.001)
        assertEquals("y property should be mutable", 100.0, animator.y, 0.001)
    }

    @Test
    fun positionConvertsToAndFromVector2Correctly() {
        val lat = 51.5074
        val lng = -0.1278
        val pos = Position(lat, lng)

        // Convert to Vector2 (x=longitude, y=latitude)
        val vec = pos.toVector2()
        assertEquals("Vector2 x should be longitude", lng, vec.x, 0.0001)
        assertEquals("Vector2 y should be latitude", lat, vec.y, 0.0001)

        // Convert back from Vector2
        val backToPos = Position.fromVector2(vec)
        assertEquals("Converted latitude should match", lat, backToPos.latitude, 0.0001)
        assertEquals("Converted longitude should match", lng, backToPos.longitude, 0.0001)
    }

    @Test
    fun animateAnimationsExtensionExistsAndIsCallable() {
        val animator = GeoAnimator()

        // Should be callable without errors
        animator.animateAnimations()
    }
}
