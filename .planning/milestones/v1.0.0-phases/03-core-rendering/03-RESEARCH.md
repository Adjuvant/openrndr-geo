# Phase 03: Core Rendering - Research

**Researched:** 2026-02-22
**Domain:** OpenRNDR drawing APIs, styling DSL, performance patterns
**Confidence:** MEDIUM

## Summary

Phase 3 focuses on creating a styling DSL for rendering geo primitives (Point, LineString, Polygon) with configurable appearance. The research reveals that OpenRNDR provides a comprehensive drawing API with fill/stroke properties, line caps/joins, and ColorRGBa for color operations. The DSL can leverage Kotlin's invoke() operator pattern already established in the project (see ProjectionMercator). A key gap identified: OpenRNDR has no built-in dash pattern API for dashed lines.

**Key research findings:**
- OpenRNDR's Drawer provides fill, stroke, strokeWeight, lineCap, lineJoin, miterLimit for styling shapes
- LineCap (BUTT, ROUND, SQUARE) and LineJoin (MITER, ROUND, BEVEL) match user requirements
- ColorRGBa supports alpha channels via `withAlpha()` method for opacity
- No built-in dash pattern support - would require custom shader implementation
- Mutable Style objects with invoke() operator following existing ProjectionMercator pattern
- Zero-allocation achieved through mutable state updates, immutable Style objects require per-frame allocation

**Primary recommendation:** Use OpenRNDR's native Drawer properties (fill, stroke, strokeWeight, lineCap, lineJoin) combined with a mutable Style class using invoke() operator. Implement dash patterns as v2 feature (requires custom shaders). Use ColorRGBa.withAlpha for transparency.

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| OpenRNDR Drawer | 0.4.5+ | Drawing primitives, shape styling | Native drawing API, built-in properties, handles GPU rendering |
| OpenRNDR Shape/ShapeContour | 0.4.5+ | Complex shapes for polygons | Provides contour methods, fill/stroke operations |
| Kotlin invoke() operator | 1.9+ | DSL syntax | Type-safe builders, lambda with receiver, already used in project |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| OpenRNDR vertexBuffer | 0.4.5+ | Batch rendering (advanced) | For rendering thousands of primitives with per-vertex attributes |
| OpenRNDR ColorRGBa | 0.4.5+ | Color operations with alpha | All color/opacity operations |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| invoke() operator DSL | Extension functions only | invoke() enables cleaner syntax like `Style { color = RED }` |
| Mutable Style | Immutable Style copy | Mutable avoids per-frame allocation for animation (critical per CONTEXT.md) |
| OpenRNDR Drawer | Manual OpenGL calls | OpenRNDR simplifies GPU operations, cross-platform |

**Installation:**
```kotlin
// Already included in OpenRNDR core - no new dependencies needed
implementation("org.openrndr:openrndr-draw:${openrndrVersion}")
implementation("org.openrndr:openrndr-shape:${openrndrVersion}")
```

## Architecture Patterns

### Recommended Project Structure
```
src/
├── render/
│   ├── Style.kt                    # Style class with invoke() operator
│   ├── StyleDefaults.kt            # Default styles per geometry type
│  ── Shape.kt                      # Shape enum (Circle, Square, Triangle)
│   ├── renderers/
│   │   ├── PointRenderer.kt        # Point drawing logic
│   │   ├── LineRenderer.kt         # LineString drawing logic
│   │   └── PolygonRenderer.kt      # Polygon drawing logic
│   └── internal/
│       ├── PointRendererInternal.kt
│       ├── LineRendererInternal.kt
│       └── PolygonRendererInternal.kt
```

### Pattern 1: Immutable Style Configuration with Mutable State
**What:** Style class holds mutable properties for animation, configured via invoke() operator
**When to use:** When users need real-time animation of appearance properties
**Example:**
```kotlin
// Source: OpenRNDR Pattern (similar to ProjectionMercator)
data class Style(
    var fill: ColorRGBa? = null,
    var stroke: ColorRGBa? = ColorRGBa.WHITE,
    var strokeWeight: Double = 1.0,
    var size: Double = 5.0,
    var shape: Shape = Shape.Circle,
    var lineCap: LineCap = LineCap.BUTT,
    var lineJoin: LineJoin = LineJoin.MITER,
    var miterLimit: Double = 4.0
) {
    companion object {
        operator fun invoke(block: Style.() -> Unit): Style {
            val style = Style()
            style.block()
            return style
        }
    }
}

// Usage:
val myStyle = Style {
    fill = ColorRGBa.RED
    stroke = ColorRGBa.BLACK
    strokeWeight = 2.0
    size = 10.0
    shape = Shape.Circle
}

// For animation:
myStyle.fill = ColorRGBa.BLUE  // Mutate for performance (no allocation)
```

### Pattern 2: Style Merge with User Override Precedence
**What:** Merge system defaults with user-provided style, user wins on conflicts
**When to use:** When providing sensible defaults while allowing customization
**Example:**
```kotlin
// Source: CONTEXT.md decision - system defaults + user override precedence
fun mergeStyles(default: Style, user: Style?): Style {
    if (user == null) return default.copy()
    return Style(
        fill = user.fill ?: default.fill,
        stroke = user.stroke ?: default.stroke,
        strokeWeight = user.strokeWeight.takeIf { it != 0.0 } ?: default.strokeWeight,
        size = user.size.takeIf { it != 0.0 } ?: default.size,
        shape = user.shape,
        lineCap = user.lineCap,
        lineJoin = user.lineJoin,
        miterLimit = user.miterLimit
    )
}

// Usage:
val pointStyle = mergeStyles(defaultPointStyle, userStyle)
```

### Pattern 3: Drawing API with Drawer Context
**What:** Accept Drawer parameter to apply style before drawing geometry
**When to use:** When rendering within OpenRNDR's program.extend block
**Example:**
```kotlin
// Source: OpenRNDR Guide - circles, rectangles, lines
fun drawPoint(drawer: Drawer, screenX: Double, screenY: Double, style: Style) {
    drawer.fill = style.fill
    drawer.stroke = style.stroke
    drawer.strokeWeight = style.strokeWeight

    when (style.shape) {
        Shape.Circle -> drawer.circle(screenX, screenY, style.size)
        Shape.Square -> drawer.rectangle(
            screenX - style.size / 2,
            screenY - style.size / 2,
            style.size,
            style.size
        )
        Shape.Triangle -> {
            // Triangle using ShapeContour
            val triangle = ShapeContour.fromPoints(listOf(
                Vector2(screenX, screenY - style.size),
                Vector2(screenX - style.size / 2, screenY + style.size / 2),
                Vector2(screenX + style.size / 2, screenY + style.size / 2)
            ), true)
            drawer.contour(triangle)
        }
    }
}

fun drawLine(drawer: Drawer, startPoint: Vector2, endPoint: Vector2, style: Style) {
    drawer.stroke = style.stroke ?: ColorRGBa.WHITE
    drawer.strokeWeight = style.strokeWeight
    drawer.lineCap = style.lineCap
    drawer.lineJoin = style.lineJoin
    drawer.lineSegment(startPoint, endPoint)
}

fun drawPolygon(drawer: Drawer, contour: ShapeContour, style: Style) {
    drawer.fill = style.fill ?: ColorRGBa.WHITE.transparent()
    drawer.stroke = style.stroke ?: ColorRGBa.BLACK
    drawer.strokeWeight = style.strokeWeight
    drawer.lineCap = style.lineCap
    drawer.lineJoin = style.lineJoin
    drawer.contour(contour)
}
```

### Anti-Patterns to Avoid
- **Immutable Style with copy() allocation per frame:** Violates zero-allocation principle for animation
- **Recreating Style objects in render loop:** Causes GC pressure, frame drops
- **Duplicating OpenRNDR functionality:** Don't re-implement shape drawing - use Contour, Shape
- **Mixing 2D and 3D drawing APIs:** Line caps only work in 2D (Vector2), not 3D (Vector3)
- **Storing computed values in Style:** Should be computed at render time for dynamic updates

## Don't Hand-Roll

### Dash Patterns
**Problem:** Dashed lines with configurable dash patterns
**Don't Build:** Manual dash line computation, custom line segment splitting
**Use Instead:** Custom shader implementation via shadeStyle
**Why:** OpenRNDR Drawer has no built-in dash support (verified by Discourse thread). Attempting manual dash computation requires complex line segment math, performance overhead, and doesn't integrate with OpenRNDR's GPU pipeline. Shade style approach leverages fragment shader for GPU-accelerated dashing.

**Implementation approach (v2):**
```kotlin
// NOT for v1 - requires GLSL shader knowledge
drawer.shadeStyle = shadeStyle {
    // Fragment shader for dash pattern
    // Requires uv coordinates, tiling, line length computation
    // Beyond scope of v1
}
```

### Line Caps/Joins for 2D vs 3D
**Problem:** Line caps not applied when using Vector3 vertex buffers
**Don't Build:** Workarounds for line caps in vector-based batch rendering
**Use Instead:** Stick to 2D drawing (Vector2) for styled lines with caps
**Why:** OpenRNDR Discourse thread confirms line caps only apply in 2D context. Using Drawer.lineSegments() with Vector3 bypasses line cap rendering (anoniim, 2023).

**Key insight:** For v1, use simple drawer.lineSegment() calls for styled lines. Batch rendering with different styles is advanced user concern (vertexBuffer programming).

### Color with Alpha
**Problem:** Managing opacity/transparency
**Don't Build:** Separate opacity property, alpha channel computation
**Use Instead:** ColorRGBa.withAlpha() method
**Why:** OpenRNDR provides built-in alpha support in ColorRGBa. Separate opacity property adds redundancy, conflicts with alpha channel.

## Common Pitfalls

### Pitfall 1: Zero-Allocation vs DSL Comfort
**What goes wrong:** Immutable Style with copy() creates new objects per frame, triggering GC pressure
**Why it happens:** Kotlin data classes encourage immutability, but CONTEXT.md requires mutable state for animation performance
**How to avoid:**
- Use `var` properties in Style class (not val)
- Mutate properties directly: `style.size += 1.0` (no allocation)
- Avoid `style.copy(size = newSize)` pattern in hot paths (allocates)
- Only use Style as mutable configuration object

**Warning signs:** Performance profiling shows allocation in render loop, GC pauses causing frame drops

### Pitfall 2: Dashed Lines Not Supported
**What goes wrong:** API doesn't have `dashPattern` property like other drawing libraries
**Why it happens:** OpenRNDR Drawer doesn't expose dash patterns (confirmed by Discourse thread 2023)
**How to avoid:**
- Document dash patterns as v2 feature
- Provide alternative: solid lines with weight/color
- Add note about custom shader implementation for advanced users

**Warning signs:** Users asking for dashed lines, dashed line code from other frameworks doesn't work

### Pitfall 3: Line Caps in 3D Rendering
**What goes wrong:** Line caps don't appear when using vector-based batch rendering with Vertex3
**Why it happens:** Line caps only apply to 2D drawing (Vector2), not GPU vertex buffers (anoniim, 2023)
**How to avoid:**
- Use drawer.lineSegment() for styled lines with caps (v1)
- Document limitation for batch rendering (v2 via vertexBuffer)
- Provide clear API distinction between `drawLine()` (styled) and `drawLines()` (batch)

**Warning signs:** Lines don't have round caps when using Vector3 coordinates

### Pitfall 4: Contour Construction Errors
**What goes wrong:** ShapeContour not closed, segments don't connect, fill not applied
**Why it happens:** ShapeContour.fromPoints() requires proper point ordering, `closed = true` for polygons
**How to avoid:**
- Always use `closed = true` for polygons (shapes that should be filled)
- For triangles: provide vertices in clockwise or counter-clockwise order
- For LineString: use `closed = false`, or close path manually with duplicate start point
- Test contours with `contour.bounds` to verify structure

**Warning signs:** Fill color not applied, polygon not filled, only outline visible

### Pitfall 5: Style Object Allocation in Loop
**What goes wrong:** Creating new Style objects for each feature in feature loop
**Why it happens:** Convenience of Style { ... } syntax encourages per-feature creation
**How to avoid:**
- Reuse single Style object, mutate properties per feature
- Use style.apply { fill = ... } to mutate without allocation
- Only create Style objects when truly needed (different property sets)

**Warning signs:** Performance drops with many features, allocation profiling shows Style object churn

## Code Examples

### Style Class with invoke() Operator
```kotlin
// Source: Existing ProjectionMercator pattern
data class Style(
    var fill: ColorRGBa? = null,
    var stroke: ColorRGBa? = ColorRGBa.WHITE,
    var strokeWeight: Double = 1.0,
    var size: Double = 5.0,
    var shape: Shape = Shape.Circle,
    var lineCap: LineCap = LineCap.BUTT,
    var lineJoin: LineJoin = LineJoin.MITER,
    var miterLimit: Double = 4.0
) {
    companion object {
        operator fun invoke(block: Style.() -> Unit): Style {
            val style = Style()
            style.block()
            return style
        }
    }

    // Convenience for creating transparent versions
    companion object {
        fun transparent() = Style().apply {
            fill = ColorRGBa.WHITE.withAlpha(0.5)
            stroke = ColorRGBa.WHITE.withAlpha(0.5)
        }
    }
}

enum class Shape {
    Circle, Square, Triangle
}
```

### Point Rendering with ShapeSwitching
```kotlin
// Source: OpenRNDR Guide - drawing circles, squares
fun drawPoint(drawer: Drawer, screenX: Double, screenY: Double, style: Style) {
    drawer.fill = style.fill
    drawer.stroke = style.stroke
    drawer.strokeWeight = style.strokeWeight

    when (style.shape) {
        Shape.Circle -> drawer.circle(screenX, screenY, style.size / 2)
        Shape.Square -> drawer.rectangle(
            screenX - style.size / 2,
            screenY - style / 2,
            style.size,
            style.size
        )
        Shape.Triangle -> {
            // Equilateral triangle
            val height = style.size * kotlin.math.sqrt(3.0) / 2
            val triangle = ShapeContour.fromPoints(listOf(
                Vector2(screenX, screenY - height / 2),
                Vector2(screenX - style.size / 2, screenY + height / 2),
                Vector2(screenX + style.size / 2, screenY + height / 2)
            ), true)
            drawer.contour(triangle)
        }
    }
}
```

### LineString Rendering for Multi-Segment Lines
```kotlin
// Source: OpenRNDR Guide - lineStrip
fun drawLineString(drawer: Drawer, points: List<Vector2>, style: Style) {
    if (points.size < 2) return

    drawer.stroke = style.stroke ?: ColorRGBa.WHITE
    drawer.strokeWeight = style.strokeWeight
    drawer.lineCap = style.lineCap
    drawer.lineJoin = style.lineJoin

    // LineStrip connects points sequentially
    drawer.lineStrip(points.map { it.vector3(z = 0.0) })
}
```

### Polygon Rendering with Fill and Stroke
```kotlin
// Source: OpenRNDR Guide - contours, shapes
fun drawPolygon(drawer: Drawer, points: List<Vector2>, style: Style) {
    if (points.size < 3) return

    drawer.fill = style.fill ?: ColorRGBa.WHITE transparent()
    drawer.stroke = style.stroke ?: ColorRGBa.BLACK
    drawer.strokeWeight = style.strokeWeight
    drawer.lineCap = style.lineCap
    drawer.lineJoin = style.lineJoin

    // Create closed contour from points
    val contour = ShapeContour.fromPoints(points, closed = true)
    drawer.contour(contour)
}

// Helper: transparent fill
fun ColorRGBa.transparent(alpha: Double = 0.5) = this.withAlpha(alpha)
```

### Style Defaults per Geometry Type
```kotlin
// Source: CONTEXT.md decision - system defaults for geometry types
object StyleDefaults {
    val defaultPointStyle = Style(
        fill = ColorRGBa.WHITE,
        stroke = ColorRGBa.WHITE,
        strokeWeight = 1.0,
        size = 5.0,
        shape = Shape.Circle
    )

    val defaultLineStyle = Style(
        fill = null,
        stroke = ColorRGBa.WHITE,
        strokeWeight = 1.0,
        lineCap = LineCap.BUTT,
        lineJoin = LineJoin.MITER
    )

    val defaultPolygonStyle = Style(
        fill = ColorRGBa.WHITE.withAlpha(0.0), // No fill by default
        stroke = ColorRGBa.WHITE,
        strokeWeight = 1.0,
        lineCap = LineCap.BUTT,
        lineJoin = LineJoin.MITER
    )
}
```

### Performance: Reusing Style Objects
```kotlin
// Source: CONTEXT.md - zero-allocation for animation
// BAD: Allocates new Style each frame
features.forEach { feature ->
    val style = Style {  // Allocation!
        fill = feature.properties["color"] as ColorRGBa
        size = (feature.properties["size"] as Double)
    }
    drawPoint(drawer, feature.screenLocation, style)
}

// GOOD: Mutate single Style object (zero allocation)
features.forEach { feature ->
    style.apply {  // No allocation
        fill = feature.properties["color"] as ColorRGBa
        size = (feature.properties["size"] as Double)
    }
    drawPoint(drawer, feature.screenLocation, style)
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Manual OpenGL calls | OpenRNDR Drawer abstraction | 2016+ | GPU-accelerated, cross-platform, simpler API |
| Immutable config objects | Mutable DSL with invoke() | 2020s (Kotlin) | Cleaner syntax, performance-friendly for animation |
| Separate alpha property | ColorRGBa.withAlpha() | OpenRNDR from start | Built-in support, no redundancy |
| Custom dash line math | Shade style dash patterns (v2) | Advanced users | GPU-accelerated, shader-based flexibility |

**Deprecated/outdated:**
- **OpenGL/DirectX manual calls:** OpenRNDR provides simpler, cross-platform abstraction
- **Manual alpha computations:** ColorRGBa.withAlpha() supersedes manual RGB math
- **Per-geometry Style allocation:** Violates zero-allocation principle, causes GC pressure

## Open Questions

### 1. Dashed Lines Implementation (v2)
**What we know:** OpenRNDR Drawer has no dash pattern API. Discourse thread 2023 confirms this gap.
**What's unclear:** Best GLSL shader pattern for dash patterns, performance characteristics.

**Options:**
1. **Custom shadeStyle:** Programmatic dash in fragment shader (requires GLSL knowledge)
2. **Manual segment splitting:** Compute dashed line segments CPU-side (expensive)
3. **Defer to v2:** Note requirement, skip for v1 (recommended)

**Recommendation:** Mark dashed lines as v2 feature with note about shadeStyle implementation option for advanced users.

### 2. Shape Enum Extensibility
**What we know:** Circle, Square, Triangle required by CONTEXT.md.
**What's unclear:** Should shapes be extensible by users in v1, or v2 feature?

**Recommendation:** Implement sealed enum for v1 (Circle, Square, Triangle). Consider shape builder pattern for custom shapes in v2.

### 3. Performance for Large Datasets
**What we know:**CONTEXT.md requires zero-allocation. VertexBuffer can batch thousands of primitives (Discourse 2023).
**What's unclear:** Optimal batching strategy for rendering 1000+ features with different styles.

**Options:**
1. **Group by style:** Batch render features sharing same style (reduces Drawer state changes)
2. **Just-in-time style:** Apply style per feature (simpler, but slower for many features)
3. **Hybrid:** Group when possible, fallback to per-feature

**Recommendation:** Start with simple per-feature approach with mutable Style objects (optimized for v1 animation). Add style batching in v2 if profiling shows performance issues.

## Sources

### Primary (HIGH confidence)
- OpenRNDR Guide "Managing draw style" - Drawer properties, lineCap, lineJoin, ColorRGBa
- OpenRNDR Guide "Curves and shapes" - ShapeContour, Shape classes, drawing APIs
- OpenRNDR Guide "Drawing circles, rectangles and lines" - Primitive drawing patterns
- OpenRNDR API Documentation - Drawer class properties, strokeWeight, fill, stroke
- OpenRNDR Guide "Color" - ColorRGBa.withAlpha() method for opacity

### Secondary (MEDIUM confidence)
- OpenRNDR Discourse Thread "How to draw batched lines with different style?" (2023) - dash patterns not available, line caps 2D-only limitation
- Kotlin Lang Docs "Type-safe builders" - invoke() operator patterns, function types with receivers
- Medium "Kotlin DSLs in 2026" (2006-01-19) - scope control, @DslMarker, DSL patterns

### Tertiary (LOW confidence)
- Web search results for performance patterns (2026) - general principles, not OpenRNDR-specific
- Code search for dash patterns - confirmed OpenRNDR doesn't provide built-in support

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Verified OpenRNDR Drawer APIs from official guide and API docs
- Architecture: MEDIUM - Style invoke() pattern from existing code, dash patterns limitation from Discourse (community source)
- Pitfalls: MEDIUM - Zero-allocation from CONTEXT.md, line caps limitation from Discourse (user-reported but consistent)

**Research date:** 2026-02-22
**Valid until:** 60 days for OpenRNDR APIs (stable framework), 30 days for dash patterns (v2 feature planning)

---

*Phase: 03-core-rendering*
*Research: OpenRNDR drawing APIs, styling DSL, performance patterns*