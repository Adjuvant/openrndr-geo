# Phase 7: Data Inspection - Research

**Researched:** 2026-02-26
**Domain:** Kotlin console output formatting, geospatial data inspection
**Confidence:** HIGH

## Summary

This phase implements `printSummary()` on GeoSource - a diagnostic utility for understanding geo data before rendering. The method provides console-based data inspection inspired by pandas DataFrame.summary(), showing feature count, bounds, CRS, geometry type distribution, memory estimate, and property keys/types.

The implementation is straightforward Kotlin string formatting using standard `println()`. No external libraries needed. The existing codebase already has all the data structures (Feature, Geometry sealed class, Bounds, CRS enum) needed to collect and display summary information.

**Primary recommendation:** Implement `printSummary()` as an extension method or instance method on GeoSource that iterates features once, collects statistics into a data class, then formats output using Kotlin string templates with visual separators.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- Print to console only — no programmatic return value (Unit return)
- Pandas DataFrame.summary() style — well-structured, readable format
- Output goes to stdout via `println()` (project doesn't use logging framework)
- Show property **keys + types only** — do NOT include sample values
- Single `printSummary()` method — no quick/detailed modes
- Method must iterate through features once to collect geometry types and bounds
- Memory estimate based on feature count × average feature size
- Instance method on GeoSource: `source.printSummary()`
- Method name makes side effect clear (prints to console)

### OpenCode's Discretion
- Output layout and visual formatting — "make it clean"
- How to format geometry type distribution (counts vs percentages vs both)
- Memory estimation formula (rough approximation acceptable)
- Section ordering and visual separators

### Deferred Ideas (OUT OF SCOPE)
- Property sample values in summary — out of scope (user can query separately)
- Return summary as data class for programmatic access — different use case
- Multiple verbosity levels (quick vs detailed) — not needed for v1.2.0
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| INSP-01 | User can call `GeoSource.summary()` to get feature count, bounds, CRS, geometry type distribution | Use existing `countFeatures()`, `totalBoundingBox()`, `crs` property; iterate features to count geometry types via sealed class `when` |
| INSP-02 | User can see memory footprint estimate in summary output | Estimate based on feature count × average bytes per geometry type (Point: ~40B, LineString: ~80B+coord, Polygon: ~120B+coord) |
| INSP-03 | User can inspect property keys and sample values from features | Collect all property keys from features; infer types from values using Kotlin `when (value)` with type checks |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Kotlin stdlib | 2.0 | String formatting, println | Project standard, no dependencies needed |
| JUnit | 4.x | Unit testing | Existing test framework in project |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| None | - | - | Pure Kotlin stdlib is sufficient |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| println() | Logging framework (SLF4J) | Adds complexity; user wants console output, not logged output |
| Custom formatting | kotlin-logging | Overkill for diagnostic method; project pattern is direct println |

**Installation:**
None required - uses existing Kotlin standard library.

## Architecture Patterns

### Recommended Implementation Structure
```
src/main/kotlin/geo/
├── GeoSource.kt           # Add printSummary() method here
└── GeoSummary.kt          # (optional) Summary data class + formatting logic
```

### Pattern 1: Single-Pass Statistics Collection
**What:** Iterate features once, collect all statistics into a mutable accumulator, then format output.
**When to use:** When iterating a Sequence that may be expensive (lazy, from file, etc.)
**Example:**
```kotlin
// Collect statistics in single pass
data class SummaryStats(
    val featureCount: Long,
    val bounds: Bounds,
    val geometryCounts: Map<String, Int>,
    val propertyTypes: Map<String, String>,
    val estimatedBytes: Long
)

fun GeoSource.collectStats(): SummaryStats {
    var count = 0L
    var bounds = Bounds.empty()
    val geomCounts = mutableMapOf<String, Int>()
    val propTypes = mutableMapOf<String, String>()
    var bytes = 0L
    
    features.forEach { feature ->
        count++
        bounds = bounds.expandToInclude(feature.boundingBox)
        val geomType = feature.geometry::class.simpleName ?: "Unknown"
        geomCounts[geomType] = (geomCounts[geomType] ?: 0) + 1
        bytes += estimateFeatureBytes(feature)
        feature.properties.forEach { (key, value) ->
            propTypes[key] = value?.let { inferType(it) } ?: "null"
        }
    }
    return SummaryStats(count, bounds, geomCounts, propTypes, bytes)
}
```

### Pattern 2: Formatted Console Output
**What:** Use Kotlin string templates with visual separators for clean, scannable output.
**When to use:** Diagnostic/inspection methods where readability matters.
**Example:**
```kotlin
fun printSummary() {
    val stats = collectStats()
    
    println("┌${"─".repeat(50)}┐")
    println("│ GeoSource Summary".padEnd(51) + "│")
    println("├${"─".repeat(50)}┤")
    println("│ Features:    ${stats.featureCount}".padEnd(51) + "│")
    println("│ CRS:         ${crs}".padEnd(51) + "│")
    println("│ Bounds:      [${stats.bounds.minX.format(2)}, ${stats.bounds.minY.format(2)}] → [${stats.bounds.maxX.format(2)}, ${stats.bounds.maxY.format(2)}]".padEnd(51) + "│")
    println("├${"─".repeat(50)}┤")
    println("│ Geometry Types:".padEnd(51) + "│")
    stats.geometryCounts.forEach { (type, count) ->
        println("│   $type: $count".padEnd(51) + "│")
    }
    println("├${"─".repeat(50)}┤")
    println("│ Memory:      ~${formatBytes(stats.estimatedBytes)}".padEnd(51) + "│")
    println("└${"─".repeat(50)}┘")
}
```

### Pattern 3: Type Inference for Properties
**What:** Use Kotlin smart casting and `when` to infer property types from values.
**When to use:** When properties are `Map<String, Any?>` and you want readable type names.
**Example:**
```kotlin
fun inferType(value: Any?): String = when (value) {
    null -> "null"
    is String -> "String"
    is Int -> "Int"
    is Long -> "Long"
    is Double -> "Double"
    is Float -> "Float"
    is Boolean -> "Boolean"
    is List<*> -> "List"
    is Map<*, *> -> "Map"
    else -> value::class.simpleName ?: "Unknown"
}
```

### Anti-Patterns to Avoid
- **Multiple iterations:** Don't call `countFeatures()` then iterate again for bounds then again for types. One pass.
- **Materializing for summary:** Don't call `listFeatures()` just to summarize - keep lazy evaluation.
- **Complex formatting libraries:** Don't add table-formatting dependencies - stdlib string templates are sufficient.
- **Returning formatted string:** User wants `Unit` return with side effect to console.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Type inference | Custom reflection-based type system | Kotlin `when` with `is` checks | Simple, fast, covers common cases |
| Memory estimation | Precise byte counting | Rough approximation (count × avg) | Precision not needed for diagnostic output |
| Table formatting | External table library | String templates + padding | Zero dependencies, sufficient for console |

**Key insight:** This is a diagnostic utility, not a core feature. Simplicity and readability trump precision and configurability.

## Common Pitfalls

### Pitfall 1: Consuming Lazy Sequence Multiple Times
**What goes wrong:** Calling `features.count()` then `features.forEach()` fails on single-use sequences.
**Why it happens:** GeoSource.features is a Sequence that may be backed by file reading or other single-use sources.
**How to avoid:** Single pass iteration - collect all stats in one `forEach` loop.
**Warning signs:** `IllegalStateException: This sequence can be consumed only once.`

### Pitfall 2: Empty Source Handling
**What goes wrong:** Division by zero or NaN in calculations, ugly output for empty datasets.
**Why it happens:** Bounds.empty() returns NaN values, count is 0.
**How to avoid:** Check `isEmpty()` first, return early with clear "Empty GeoSource" message.
**Warning signs:** Output shows "NaN" or crashes on empty test data.

### Pitfall 3: Property Type Conflicts
**What goes wrong:** Same property key has different types across features (e.g., "id" is Int in some, String in others).
**Why it happens:** GeoJSON is schemaless; real-world data is messy.
**How to avoid:** Show "mixed" or the first encountered type; don't try to reconcile conflicts.
**Warning signs:** Complex type inference logic, crashes on heterogeneous data.

### Pitfall 4: Memory Estimate Inaccuracy
**What goes wrong:** Estimate is wildly off (orders of magnitude).
**Why it happens:** Geometry sizes vary dramatically (simple point vs complex polygon with 1000 vertices).
**How to avoid:** Use coarse ranges (bytes per coordinate × coordinate count), prefix with "~" to indicate approximation.
**Warning signs:** Spending too much time on "accurate" estimation.

## Code Examples

### Complete printSummary() Implementation
```kotlin
// In GeoSource.kt or GeoSummary.kt

/**
 * Print a summary of this GeoSource to the console.
 * Shows feature count, bounds, CRS, geometry types, memory estimate, and property keys.
 * 
 * ## Usage
 * ```kotlin
 * val source = geoSource("data.json")
 * source.printSummary()
 * ```
 */
fun GeoSource.printSummary() {
    if (isEmpty()) {
        println("GeoSource: Empty (no features)")
        return
    }
    
    // Single-pass collection
    var count = 0L
    var bounds = Bounds.empty()
    val geomCounts = mutableMapOf<String, Int>()
    val propTypes = mutableMapOf<String, String>()
    var coordCount = 0
    
    features.forEach { feature ->
        count++
        bounds = bounds.expandToInclude(feature.boundingBox)
        
        val geomType = when (val g = feature.geometry) {
            is Point -> "Point"
            is LineString -> "LineString"
            is Polygon -> "Polygon"
            is MultiPoint -> "MultiPoint"
            is MultiLineString -> "MultiLineString"
            is MultiPolygon -> "MultiPolygon"
        }
        geomCounts[geomType] = (geomCounts[geomType] ?: 0) + 1
        coordCount += countCoordinates(feature.geometry)
        
        feature.properties.forEach { (key, value) ->
            if (key !in propTypes) {
                propTypes[key] = inferTypeName(value)
            }
        }
    }
    
    // Format output
    val separator = "─".repeat(52)
    println("┌$separator┐")
    println("│ ${"GeoSource Summary".center(50)} │")
    println("├$separator┤")
    println("│ Features:    ${count.toString().padEnd(36)} │")
    println("│ CRS:         ${crs.padEnd(36)} │")
    println("│ Bounds:      ${formatBounds(bounds).padEnd(36)} │")
    println("├$separator┤")
    println("│ ${"Geometry Types:".padEnd(50)} │")
    geomCounts.toSortedMap().forEach { (type, cnt) ->
        val pct = (cnt.toDouble() / count * 100).toInt()
        println("│   ${"$type: $cnt ($pct%)".padEnd(48)} │")
    }
    println("├$separator┤")
    println("│ Memory:      ${formatMemory(coordCount).padEnd(36)} │")
    println("├$separator┤")
    println("│ ${"Properties (${propTypes.size} keys):".padEnd(50)} │")
    propTypes.toSortedMap().take(10).forEach { (key, type) ->
        println("│   ${"$key: $type".padEnd(48)} │")
    }
    if (propTypes.size > 10) {
        println("│   ${"... and ${propTypes.size - 10} more".padEnd(48)} │")
    }
    println("└$separator┘")
}

private fun countCoordinates(geom: Geometry): Int = when (geom) {
    is Point -> 1
    is LineString -> geom.points.size
    is Polygon -> geom.exterior.size + geom.interiors.sumOf { it.size }
    is MultiPoint -> geom.points.size
    is MultiLineString -> geom.lineStrings.sumOf { it.points.size }
    is MultiPolygon -> geom.polygons.sumOf { p -> 
        p.exterior.size + p.interiors.sumOf { it.size } 
    }
}

private fun inferTypeName(value: Any?): String = when (value) {
    null -> "null"
    is String -> "String"
    is Int -> "Int"
    is Long -> "Long"
    is Double -> "Double"
    is Float -> "Float"
    is Boolean -> "Boolean"
    is Number -> "Number"
    is List<*> -> "List"
    is Map<*, *> -> "Map"
    else -> value::class.simpleName ?: "Any"
}

private fun formatBounds(bounds: Bounds): String {
    if (bounds.isEmpty()) return "N/A"
    return "[${bounds.minX.format(2)}, ${bounds.minY.format(2)}] → [${bounds.maxX.format(2)}, ${bounds.maxY.format(2)}]"
}

private fun formatMemory(coordCount: Int): String {
    // Rough estimate: 16 bytes per coordinate (2 doubles) + object overhead
    val bytes = coordCount * 24L + 100L // ~24 bytes per coord + overhead
    return when {
        bytes < 1024 -> "~$bytes B"
        bytes < 1024 * 1024 -> "~${bytes / 1024} KB"
        else -> "~${bytes / (1024 * 1024)} MB"
    }
}

private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
private fun String.center(width: Int) = padStart((width + length) / 2).padEnd(width)
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Logging framework output | Direct println() | Project convention | Simpler, matches creative coding workflow |
| Multiple iterations | Single-pass collection | Best practice | Efficient for lazy sequences |

**Deprecated/outdated:**
- None relevant - this is new functionality

## Open Questions

1. **Property display limit**
   - What we know: Properties can have many keys (10+ common in real data)
   - What's unclear: Optimal number to show before truncating
   - Recommendation: Show first 10, indicate "... and N more" for remainder

2. **Geometry type ordering**
   - What we know: 6 geometry types (Point, LineString, Polygon, Multi*)
   - What's unclear: Should output be sorted by count or by type hierarchy
   - Recommendation: Alphabetical sort is simplest and predictable

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4.x |
| Config file | None (standard Gradle test) |
| Quick run command | `./gradlew test --tests "geo.GeoSourceSummaryTest" -x shadowJar` |
| Full suite command | `./gradlew test -x shadowJar` |
| Estimated runtime | ~5 seconds |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| INSP-01 | Feature count, bounds, CRS, geometry type distribution | unit | `./gradlew test --tests "geo.GeoSourceSummaryTest::testSummaryIncludesFeatureCount"` | ❌ Wave 0 gap |
| INSP-02 | Memory footprint estimate | unit | `./gradlew test --tests "geo.GeoSourceSummaryTest::testSummaryIncludesMemoryEstimate"` | ❌ Wave 0 gap |
| INSP-03 | Property keys and types | unit | `./gradlew test --tests "geo.GeoSourceSummaryTest::testSummaryIncludesPropertyKeys"` | ❌ Wave 0 gap |

### Nyquist Sampling Rate
- **Minimum sample interval:** After every committed task → run: `./gradlew test --tests "geo.GeoSourceSummaryTest" -x shadowJar`
- **Full suite trigger:** Before merging final task of any plan wave
- **Phase-complete gate:** Full suite green before `/gsd-verify-work` runs
- **Estimated feedback latency per task:** ~8 seconds

### Wave 0 Gaps (must be created before implementation)
- [ ] `src/test/kotlin/geo/GeoSourceSummaryTest.kt` — covers INSP-01, INSP-02, INSP-03
- [ ] Test fixtures: Sample GeoJSON with mixed geometry types and properties

**Test Strategy:** Since printSummary() outputs to console, tests should:
1. Test the statistics collection logic (programmatically verifiable)
2. Optionally capture stdout for output format verification
3. Test edge cases: empty source, single feature, mixed geometries, many properties

## Sources

### Primary (HIGH confidence)
- Existing codebase: GeoSource.kt, Geometry.kt, Feature.kt, Bounds.kt, CRS.kt - all reviewed
- Kotlin 2.0 stdlib patterns for string formatting and type checking

### Secondary (MEDIUM confidence)
- Project conventions: println for output, JUnit for testing (established in GeoSourceTest.kt)

### Tertiary (LOW confidence)
- None - implementation is straightforward Kotlin stdlib

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Pure Kotlin stdlib, no external dependencies
- Architecture: HIGH - Clear pattern from existing codebase
- Pitfalls: HIGH - Common Kotlin Sequence pitfalls well-documented

**Research date:** 2026-02-26
**Valid until:** 30 days - stable Kotlin patterns
