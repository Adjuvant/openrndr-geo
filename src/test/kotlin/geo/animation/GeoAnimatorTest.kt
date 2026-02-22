package geo.animation

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.animatable.easing.Easing

/**
 * Synthetic tests for GeoAnimator infrastructure.
 *
 * These tests verify the basic setup of the animation system
 * without requiring a full OpenRNDR application context.
 */
class GeoAnimatorTest {

    @Test
    fun `GeoAnimator singleton returns same instance`() {
        val animator1 = GeoAnimator.instance
        val animator2 = GeoAnimator.instance
        assertSame("GeoAnimator should be a singleton", animator1, animator2)
    }

    @Test
    fun `GeoAnimator extends Animatable`() {
        val animator = GeoAnimator.instance
        // Verify we can call Animatable methods
        assertFalse("Fresh animator should have no animations", animator.hasAnimations())
        // Cancel should not throw
        animator.cancel()
    }

    @Test
    fun `GeoAnimator has mutable animated properties`() {
        val animator = GeoAnimator.instance
        
        // Verify properties exist and are mutable
        animator.x = 100.0
        animator.y = 200.0
        animator.progress = 0.5
        
        assertEquals(100.0, animator.x, 0.001)
        assertEquals(200.0, animator.y, 0.001)
        assertEquals(0.5, animator.progress, 0.001)
    }

    @Test
    fun `Easing convenience functions return valid enum values`() {
        // Test all convenience functions return non-null Easing values
        assertNotNull(linear())
        assertNotNull(none())
        assertNotNull(easeInOut())
        assertNotNull(easeOut())
        assertNotNull(easeIn())
        assertNotNull(sineInOut())
        assertNotNull(quadInOut())
        assertNotNull(quartInOut())
        assertNotNull(quadOut())
        assertNotNull(quadIn())
        assertNotNull(sineOut())
        assertNotNull(sineIn())
        assertNotNull(cubicInOut())
        assertNotNull(cubicOut())
        assertNotNull(cubicIn())
    }

    @Test
    fun `linear and none are equivalent`() {
        assertEquals("linear() and none() should return same Easing", linear(), none())
        assertEquals("linear() should return Easing.None", Easing.None, linear())
    }

    @Test
    fun `ease functions return correct Easing enum`() {
        assertEquals(Easing.CubicInOut, easeInOut())
        assertEquals(Easing.CubicOut, easeOut())
        assertEquals(Easing.CubicIn, easeIn())
        assertEquals(Easing.SineInOut, sineInOut())
        assertEquals(Easing.QuadInOut, quadInOut())
        assertEquals(Easing.QuartInOut, quartInOut())
    }

    @Test
    fun `all Easing enum values are accessible`() {
        // Verify all documented enum values exist
        val expectedEasings = setOf(
            Easing.None,
            Easing.SineIn, Easing.SineOut, Easing.SineInOut,
            Easing.QuadIn, Easing.QuadOut, Easing.QuadInOut,
            Easing.CubicIn, Easing.CubicOut, Easing.CubicInOut,
            Easing.QuartIn, Easing.QuartOut, Easing.QuartInOut
        )
        
        assertEquals("Should have exactly 13 easing values", 13, expectedEasings.size)
    }
}
