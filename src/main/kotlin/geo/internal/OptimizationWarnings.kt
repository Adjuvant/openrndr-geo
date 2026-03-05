package geo.internal

/**
 * Console warnings for performance optimization recommendations.
 *
 * Provides helpful warnings when large geometries are loaded without
 * the optimize flag, guiding users to better performance.
 */

/**
 * Threshold for optimization warning (coordinates).
 * Geometries with more than this many coordinates trigger a warning
 * when loaded without optimize=true.
 */
internal const val OPTIMIZATION_WARNING_THRESHOLD = 5000

/**
 * Checks if an optimization warning should be displayed and prints it if needed.
 *
 * @param featureCount Number of features in the loaded data
 * @param coordinateCount Total number of coordinates across all features
 * @param optimizeFlag Whether the user enabled optimization
 */
internal fun checkOptimizationRecommendation(
    featureCount: Int,
    coordinateCount: Int,
    optimizeFlag: Boolean
) {
    // If optimization is already enabled, no warning needed
    if (optimizeFlag) return

    // Only warn for large geometries
    if (coordinateCount <= OPTIMIZATION_WARNING_THRESHOLD) return

    // Print helpful, actionable warning
    println(
        buildString {
            appendLine()
            appendLine("⚡ Performance Tip: Large geometry detected ($coordinateCount coordinates across $featureCount features).")
            appendLine("   Consider using optimize=true for better performance: loadGeoJSON(path, optimize = true)")
            appendLine()
        }
    )
}
