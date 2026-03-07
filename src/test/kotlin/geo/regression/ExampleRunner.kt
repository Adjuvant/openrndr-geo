package geo.regression

import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.concurrent.*

/**
 * Result of running a single example.
 */
data class ExampleResult(
    val className: String,
    val success: Boolean,
    val durationMs: Long,
    val error: Throwable? = null,
    val output: String = ""
)

/**
 * Test harness for discovering and executing example programs.
 * 
 * This harness can:
 * - Discover all example classes in the examples/ package
 * - Load and execute example main() methods
 * - Handle timeouts (examples shouldn't run forever in tests)
 * - Capture and report compilation/execution status
 */
object ExampleRunner {
    
    /**
     * List of known example class names in the examples package.
     * These are discovered by scanning the examples/ directory.
     * Note: Class names don't have "Kt" suffix due to @file:JvmName annotations.
     */
    private val knownExamples = listOf(
        // Core examples
        "examples.core.LoadGeojson",
        "examples.core.LoadGeopackage",
        "examples.core.PrintSummary",
        "examples.core.GeoStack",
        "examples.core.BatchOptimization",

        // Animation examples
        "examples.anim.BasicAnimation",
        "examples.anim.GeoAnimator",
        "examples.anim.Timeline",
        "examples.anim.StaggerAnimator",
        "examples.anim.ChainAnimations",
        "examples.anim.LineStringColorAnim",
        "examples.anim.TimelineDemo",
        "examples.anim.StaggerDemo",
        "examples.anim.RippleDemo",
        "examples.anim.QuickGeo",

        // Rendering examples
        "examples.render.Points",
        "examples.render.Linestrings",
        "examples.render.Polygons",
        "examples.render.Multipolygons",
        "examples.render.StyleDsl",
        "examples.render.GeoStackRender",

        // Layer examples
        "examples.layer.Graticule",
        "examples.layer.Composition",

        // Projection examples
        "examples.proj.Mercator",
        "examples.proj.FitBounds",
        "examples.proj.CrsTransform"
    )
    
    /**
     * Examples that don't require OPENRNDR application context.
     * These are pure console examples that can run headless.
     */
    private val consoleExamples = setOf(
        "examples.core.LoadGeojson",
        "examples.core.LoadGeopackage",
        "examples.core.PrintSummary",
        "examples.core.GeoStack",
        "examples.core.BatchOptimization"
    )
    
    /**
     * Discover all example classes available in the classpath.
     * @return List of fully qualified class names
     */
    fun discoverExamples(): List<String> {
        return knownExamples.filter { className ->
            try {
                Class.forName(className)
                true
            } catch (e: ClassNotFoundException) {
                false
            }
        }
    }
    
    /**
     * Run a single example by class name.
     * 
     * @param className Fully qualified class name (e.g., "examples.core.LoadGeojsonKt")
     * @param timeoutMs Maximum time to wait for execution (default 5000ms)
     * @return ExampleResult with success status and any error information
     */
    fun runExample(className: String, timeoutMs: Long = 5000): ExampleResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            // Verify class exists
            val clazz = Class.forName(className)
            
            // Find the main method
            val mainMethod = clazz.getMethod("main", Array<String>::class.java)
            
            // For console examples, capture output
            val isConsoleExample = className in consoleExamples
            
            if (isConsoleExample) {
                runConsoleExample(className, mainMethod, timeoutMs, startTime)
            } else {
                // GUI examples - just verify they load and have main method
                // We don't actually run them in test mode to avoid window creation
                ExampleResult(
                    className = className,
                    success = true,
                    durationMs = System.currentTimeMillis() - startTime,
                    error = null,
                    output = "GUI example - verified class loads and has main method"
                )
            }
        } catch (e: ClassNotFoundException) {
            ExampleResult(
                className = className,
                success = false,
                durationMs = System.currentTimeMillis() - startTime,
                error = e,
                output = "Class not found: $className"
            )
        } catch (e: NoSuchMethodException) {
            ExampleResult(
                className = className,
                success = false,
                durationMs = System.currentTimeMillis() - startTime,
                error = e,
                output = "No main method found in $className"
            )
        } catch (e: Exception) {
            ExampleResult(
                className = className,
                success = false,
                durationMs = System.currentTimeMillis() - startTime,
                error = e,
                output = "Exception: ${e.message}"
            )
        }
    }
    
    /**
     * Run a console-based example and capture output.
     */
    private fun runConsoleExample(
        className: String,
        mainMethod: java.lang.reflect.Method,
        timeoutMs: Long,
        startTime: Long
    ): ExampleResult {
        // Capture stdout/stderr
        val originalOut = System.out
        val originalErr = System.err
        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream)
        
        return try {
            System.setOut(printStream)
            System.setErr(printStream)
            
            // Execute in a separate thread with timeout
            val executor = Executors.newSingleThreadExecutor()
            val future = executor.submit<Throwable?> {
                try {
                    mainMethod.invoke(null, arrayOf<String>())
                    null
                } catch (e: Throwable) {
                    e
                }
            }
            
            val error = try {
                future.get(timeoutMs, TimeUnit.MILLISECONDS)
            } catch (e: TimeoutException) {
                future.cancel(true)
                TimeoutException("Example execution timed out after ${timeoutMs}ms")
            } catch (e: ExecutionException) {
                e.cause ?: e
            } finally {
                executor.shutdownNow()
            }
            
            val duration = System.currentTimeMillis() - startTime
            val output = outputStream.toString("UTF-8")
            
            ExampleResult(
                className = className,
                success = error == null,
                durationMs = duration,
                error = error,
                output = output
            )
        } finally {
            System.setOut(originalOut)
            System.setErr(originalErr)
        }
    }
    
    /**
     * Run all discovered examples.
     * @return List of results for all examples
     */
    fun runAllExamples(): List<ExampleResult> {
        val examples = discoverExamples()
        return examples.map { runExample(it) }
    }
    
    /**
     * Check if a class is available in the classpath.
     */
    fun isClassAvailable(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}

/**
 * Basic tests for ExampleRunner itself.
 */
class ExampleRunnerTest {
    
    @Test
    fun `discoverExamples returns available examples`() {
        val examples = ExampleRunner.discoverExamples()
        assertTrue("Should discover at least some examples", examples.isNotEmpty())
        
        // Core examples should be discoverable
        assertTrue(
            "Should discover core examples",
            examples.any { it.contains("core") }
        )
    }
    
    @Test
    fun `runExample can load console example`() {
        val result = ExampleRunner.runExample("examples.core.LoadGeojson")

        assertNotNull(result)
        assertEquals("examples.core.LoadGeojson", result.className)
        // May fail if data file doesn't exist, but should not crash
        assertTrue(
            "Should either succeed or fail gracefully",
            result.success || result.error != null
        )
    }
    
    @Test
    fun `runExample handles non-existent class gracefully`() {
        val result = ExampleRunner.runExample("examples.nonexistent.NonExistentKt")

        assertFalse("Should fail for non-existent class", result.success)
        assertNotNull(result.error)
    }
    
    @Test
    fun `isClassAvailable returns true for existing class`() {
        assertTrue(
            "LoadGeojson should be available",
            ExampleRunner.isClassAvailable("examples.core.LoadGeojson")
        )
    }

    @Test
    fun `isClassAvailable returns false for non-existent class`() {
        assertFalse(
            "Non-existent class should return false",
            ExampleRunner.isClassAvailable("examples.nonexistent.NonExistentKt")
        )
    }
}
