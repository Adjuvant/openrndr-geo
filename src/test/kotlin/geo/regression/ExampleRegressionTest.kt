package geo.regression

import org.junit.Test
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.AfterClass

/**
 * Regression tests that verify all v1.2.0 examples continue to work with
 * the Phase 11-12 optimizations (batch projection and viewport caching).
 *
 * These tests ensure:
 * - All examples compile and load successfully
 * - Console examples execute without errors
 * - GUI examples can be instantiated (don't test actual rendering)
 * - Memory usage remains bounded during test runs
 * - No breaking API changes were introduced
 *
 * ## Running Regression Tests
 *
 * Run all regression tests:
 * ```
 * ./gradlew regressionTest
 * ```
 *
 * Run specific test:
 * ```
 * ./gradlew test --tests "geo.regression.ExampleRegressionTest.testCoreExamples"
 * ```
 */
class ExampleRegressionTest {

    companion object {
        private lateinit var allExamples: List<String>
        private var baselineMemory: Long = 0

        @BeforeClass
        @JvmStatic
        fun setup() {
            allExamples = ExampleRunner.discoverExamples()
            System.gc()
            baselineMemory = getUsedMemory()
            println("=== Example Regression Test Suite ===")
            println("Discovered ${allExamples.size} examples")
            println("Baseline memory: ${baselineMemory / 1024 / 1024} MB")
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            System.gc()
            val finalMemory = getUsedMemory()
            val memoryDelta = finalMemory - baselineMemory
            println("\n=== Test Suite Complete ===")
            println("Final memory: ${finalMemory / 1024 / 1024} MB")
            println("Memory delta: ${memoryDelta / 1024 / 1024} MB")
        }

        private fun getUsedMemory(): Long {
            val runtime = Runtime.getRuntime()
            return runtime.totalMemory() - runtime.freeMemory()
        }
    }

    /**
     * Test all core examples that work with data loading and GeoStack.
     * These are console-only examples that don't require OPENRNDR.
     */
    @Test
    fun `test Core Examples`() {
        val coreExamples = listOf(
            "examples.core.LoadGeojson",
            "examples.core.LoadGeopackage",
            "examples.core.PrintSummary",
            "examples.core.GeoStack"
        )

        val results = mutableListOf<ExampleResult>()

        coreExamples.forEach { className ->
            println("\nTesting: $className")
            val result = ExampleRunner.runExample(className, timeoutMs = 10000)
            results.add(result)

            println("  Success: ${result.success}")
            println("  Duration: ${result.durationMs}ms")
            if (result.output.isNotBlank()) {
                val outputPreview = result.output.lines().take(3).joinToString("\n")
                println("  Output preview:\n$outputPreview")
            }
            if (result.error != null) {
                println("  Error: ${result.error.javaClass.simpleName}: ${result.error.message}")
            }
        }

        // Assert all core examples succeed
        val failures = results.filter { !it.success }
        if (failures.isNotEmpty()) {
            val failureMessage = failures.joinToString("\n") {
                "  - ${it.className}: ${it.error?.message ?: "Unknown error"}"
            }
            fail("Core example failures (${failures.size}):\n$failureMessage")
        }

        println("\n✓ All ${coreExamples.size} core examples passed")
    }

    /**
     * Test that animation examples can be loaded and have valid structure.
     * GUI examples are not fully executed (would require OPENRNDR context).
     */
    @Test
    fun `test Animation Examples Load`() {
        val animExamples = listOf(
            "examples.anim.BasicAnimation",
            "examples.anim.GeoAnimator",
            "examples.anim.Timeline",
            "examples.anim.StaggerAnimator",
            "examples.anim.ChainAnimations",
            "examples.anim.LineStringColorAnim",
            "examples.anim.TimelineDemo",
            "examples.anim.StaggerDemo",
            "examples.anim.RippleDemo",
            "examples.anim.QuickGeo"
        )

        testExamplesLoad(animExamples, "Animation")
    }

    /**
     * Test that rendering examples can be loaded and have valid structure.
     */
    @Test
    fun `test Rendering Examples Load`() {
        val renderExamples = listOf(
            "examples.render.Points",
            "examples.render.Linestrings",
            "examples.render.Polygons",
            "examples.render.Multipolygons",
            "examples.render.StyleDsl",
            "examples.render.GeoStackRender"
        )

        testExamplesLoad(renderExamples, "Rendering")
    }

    /**
     * Test that layer examples can be loaded and have valid structure.
     */
    @Test
    fun `test Layer Examples Load`() {
        val layerExamples = listOf(
            "examples.layer.Graticule",
            "examples.layer.Composition"
        )

        testExamplesLoad(layerExamples, "Layer")
    }

    /**
     * Test that projection examples can be loaded and have valid structure.
     */
    @Test
    fun `test Projection Examples Load`() {
        val projExamples = listOf(
            "examples.proj.Mercator",
            "examples.proj.FitBounds",
            "examples.proj.CrsTransform"
        )

        testExamplesLoad(projExamples, "Projection")
    }

    /**
     * Run all discovered examples and verify they work.
     * This is the comprehensive regression test.
     */
    @Test
    fun `test All Discovered Examples`() {
        println("\n=== Running All Discovered Examples ===")
        println("Total: ${allExamples.size} examples")

        val results = ExampleRunner.runAllExamples()

        val consoleResults = results.filter {
            it.className.startsWith("examples.core")
        }
        val guiResults = results.filter {
            !it.className.startsWith("examples.core")
        }

        println("\n--- Console Examples (${consoleResults.size}) ---")
        consoleResults.forEach { result ->
            val status = if (result.success) "✓" else "✗"
            println("  $status ${result.className} (${result.durationMs}ms)")
        }

        println("\n--- GUI Examples (${guiResults.size}) ---")
        guiResults.forEach { result ->
            val status = if (result.success) "✓" else "✗"
            println("  $status ${result.className} (${result.durationMs}ms)")
        }

        // Console examples must all pass
        val consoleFailures = consoleResults.filter { !it.success }
        assertTrue(
            "Console examples should all pass. Failures: ${consoleFailures.map { it.className }}",
            consoleFailures.isEmpty()
        )

        // GUI examples should at least load (success = true for load-only verification)
        val guiFailures = guiResults.filter { !it.success }
        assertTrue(
            "GUI examples should all load. Failures: ${guiFailures.map { it.className }}",
            guiFailures.isEmpty()
        )

        println("\n✓ All ${results.size} examples passed regression testing")
    }

    /**
     * Verify memory usage remains bounded when running examples.
     * This catches memory leaks in the optimization code.
     */
    @Test
    fun `test Memory Usage Remains Bounded`() {
        println("\n=== Memory Usage Test ===")

        val runtime = Runtime.getRuntime()

        // Get baseline
        System.gc()
        Thread.sleep(100)
        val baseline = runtime.totalMemory() - runtime.freeMemory()
        println("Baseline memory: ${baseline / 1024 / 1024} MB")

        // Run console examples multiple times
        val coreExamples = listOf(
            "examples.core.LoadGeojson",
            "examples.core.PrintSummary",
            "examples.core.GeoStack"
        )

        repeat(3) { iteration ->
            println("\nIteration ${iteration + 1}:")
            coreExamples.forEach { className ->
                ExampleRunner.runExample(className, timeoutMs = 5000)
            }

            System.gc()
            Thread.sleep(100)
            val current = runtime.totalMemory() - runtime.freeMemory()
            val delta = current - baseline
            println("  Memory delta: ${delta / 1024 / 1024} MB")

            // Memory should not grow unboundedly
            // Allow up to 100MB growth per iteration (generous for test environment)
            assertTrue(
                "Memory growth too high: ${delta / 1024 / 1024}MB (iteration ${iteration + 1})",
                delta < 100 * 1024 * 1024
            )
        }

        // Final GC and check
        System.gc()
        Thread.sleep(200)
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val finalDelta = finalMemory - baseline

        println("\nFinal memory delta after GC: ${finalDelta / 1024 / 1024} MB")

        // Memory should return close to baseline after GC
        assertTrue(
            "Memory did not return to baseline: ${finalDelta / 1024 / 1024}MB remaining",
            finalDelta < 50 * 1024 * 1024  // Allow 50MB tolerance
        )
    }

    /**
     * Verify specific examples work correctly with their expected outputs.
     */
    @Test
    fun `test LoadGeojson produces expected output`() {
        val result = ExampleRunner.runExample("examples.core.LoadGeojson", timeoutMs = 10000)

        assertTrue("LoadGeojson should succeed", result.success)
        assertTrue("Output should contain GeoJSON", result.output.contains("GeoJSON"))
        assertTrue("Output should mention file", result.output.contains("sample.geojson"))
        assertTrue("Output should contain features loaded", result.output.contains("Features loaded"))
    }

    /**
     * Verify GeoStack example works correctly.
     */
    @Test
    fun `test GeoStack example produces expected output`() {
        val result = ExampleRunner.runExample("examples.core.GeoStack", timeoutMs = 10000)

        assertTrue("GeoStack example should succeed", result.success)
        // GeoStack example should complete without errors
        assertNull("No error should occur", result.error)
    }

    // ==================== Helper Methods ====================

    /**
     * Test that a list of examples can be loaded successfully.
     * For GUI examples, this just verifies class loading, not full execution.
     */
    private fun testExamplesLoad(examples: List<String>, category: String) {
        println("\n=== Testing $category Examples ===")

        val results = examples.map { className ->
            println("Testing: $className")
            ExampleRunner.runExample(className)
        }

        val failures = results.filter { !it.success }

        if (failures.isNotEmpty()) {
            val failureMessage = failures.joinToString("\n") {
                "  - ${it.className}: ${it.error?.javaClass?.simpleName}: ${it.error?.message}"
            }
            fail("$category example load failures (${failures.size}):\n$failureMessage")
        }

        println("✓ All ${examples.size} $category examples loaded successfully")
    }
}
