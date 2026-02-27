# Phase 08: Rendering Improvements - Research

**Researched:** 2026-02-27
**Domain:** OpenRNDR Shape API for polygon rendering with interior rings (holes)
**Confidence:** HIGH

## Summary

This phase implements proper rendering of polygons with interior rings (holes) using OpenRNDR's `shape { }` builder API. The current implementation only renders exterior rings via `ShapeContour.fromPoints()`, which cannot represent holes. The solution requires using OpenRNDR's Shape class with multiple contours - the first contour defines the exterior boundary, and subsequent contours define holes that appear as transparent cutouts.

**Primary recommendation:** Create a new `writePolygonWithHoles()` function using `shape { contour { ... } contour { ... } }` builder pattern, then update the rendering pipeline to detect holes and route to the appropriate renderer.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Implementation Decisions
- Use OpenRNDR's shape builder API for rendering polygons with holes
- Shape builder requires relative moves (cursor + Vector2) rather than absolute coordinates
- Reference: https://guide.openrndr.org/drawing/curvesAndShapes.html#constructing-a-shape-using-the-shape-builder

### Visual appearance of holes
- Holes should be transparent (show background behind the polygon)
- This matches standard GIS behavior where holes cut through the fill

### Hole boundary styling
- Hole boundaries use the same stroke settings from the applied style
- No special handling needed - stroke applies to both exterior and interior rings consistently
- Breaking this out separately would require significant OpenRNDR modifications

### OpenCode's Discretion
- Coordinate transformation approach (how to convert absolute geo coordinates to relative shape builder commands)
- Clamping behavior for holes that extend beyond Mercator bounds
- Performance optimizations for complex MultiPolygons with many holes
- Sample data selection for validation

### Deferred Ideas (OUT OF SCOPE)
None — discussion stayed within phase scope
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| REND-07 | User can render Polygon features with interior rings (holes) correctly | OpenRNDR Shape API with multiple contours - see Code Examples section |
| REND-08 | User can render MultiPolygon features with interior rings clamped to projection bounds | `clampToMercator()` already exists in Geometry.kt; apply before projection |
| REND-09 | User sees correct rendering of ocean/whole-world MultiPolygon data | Mercator clamping to ±85.05112878°; ocean.geojson available for testing |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| OpenRNDR | 0.4.x | Shape/ShapeContour API | Core rendering primitives |
| org.openrndr.shape | built-in | Shape builder DSL | Native hole support via multiple contours |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| ShapeContour | built-in | Single closed path | Simple polygons without holes |
| Shape | built-in | Multiple contours with holes | Polygons with interior rings |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Shape builder | Boolean operations (compound {}) | Compound is for combining shapes, not defining holes in single polygon |
| Shape builder | Custom triangulation | Reinventing wheel; OpenRNDR handles this internally |

**Installation:**
No additional dependencies required - OpenRNDR Shape API is part of core library.

## Architecture Patterns

### Current Implementation (No Hole Support)
```
Polygon → exteriorToScreen() → List<Vector2>
                                    ↓
                          ShapeContour.fromPoints()
                                    ↓
                              drawer.contour()
```

### New Implementation (With Hole Support)
```
Polygon → exteriorToScreen() → List<Vector2>  ─┐
       → interiorsToScreen() → List<List<Vector2>> ─┼→ writePolygonWithHoles()
                                                    ↓
                                              shape {
                                                contour { exterior }
                                                contour { hole1 }
                                                contour { hole2 }
                                              }
                                                    ↓
                                              drawer.shape()
```

### Recommended Project Structure
```
src/main/kotlin/geo/render/
├── PolygonRenderer.kt      # Add writePolygonWithHoles()
├── MultiRenderer.kt        # Update drawMultiPolygon() for interiors
├── DrawerGeoExtensions.kt  # Update renderToDrawer() to pass interiors
└── render.kt               # Update drawPolygon() signature

src/main/kotlin/geo/
└── Geometry.kt             # Implement interiorsToScreen()
```

### Pattern 1: OpenRNDR Shape Builder for Holes
**What:** Creates a Shape with multiple contours where subsequent contours are holes
**When to use:** Any polygon with interior rings (holes)
**Example:**
```kotlin
// Source: https://guide.openrndr.org/drawing/curvesAndShapes.html
val s = shape {
    // First contour = exterior boundary
    contour {
        moveTo(Vector2(width / 2.0 - 120.0, height / 2.0 - 120.00))
        lineTo(cursor + Vector2(240.0, 0.0))  // Relative move
        lineTo(cursor + Vector2(0.0, 240.0))  // Relative move
        lineTo(anchor)                         // Back to start
        close()
    }
    // Second contour = hole (transparent cutout)
    contour {
        moveTo(Vector2(width / 2.0 - 80.0, height / 2.0 - 100.0))
        lineTo(cursor + Vector2(190.0, 0.0))
        lineTo(cursor + Vector2(0.0, 190.00))
        lineTo(anchor)
        close()
    }
}
drawer.shape(s)  // Renders with hole as transparent cutout
```

### Pattern 2: Converting Absolute Coordinates to Relative Moves
**What:** Transform projected screen coordinates to shape builder's relative format
**When to use:** Building contours from projected coordinate lists
**Example:**
```kotlin
fun buildContourFromAbsolutePoints(points: List<Vector2>): ShapeContour {
    require(points.size >= 3) { "Need at least 3 points for contour" }
    return contour {
        moveTo(points[0])  // Set anchor and cursor to first point
        for (i in 1 until points.size) {
            // Calculate delta from current cursor to next point
            val delta = points[i] - cursor
            lineTo(cursor + delta)  // Equivalent to lineTo(points[i])
        }
        close()
    }
}
```

**Key insight:** The shape builder's `cursor + Vector2(dx, dy)` syntax is for clarity. OpenRNDR's contour builder also accepts absolute coordinates directly in `lineTo()`, but the relative pattern is more idiomatic for the DSL.

### Anti-Patterns to Avoid
- **Using ShapeContour.fromPoints() for polygons with holes:** This creates a single contour without hole support. Use `shape { }` builder instead.
- **Creating separate drawer.contour() calls for holes:** This draws holes as filled shapes, not transparent cutouts.
- **Skipping hole coordinate projection:** Interior rings must be projected to screen space just like exterior rings.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Polygon triangulation | Custom ear-clipping or similar | OpenRNDR Shape API | Handles complex polygons, winding order, edge cases |
| Hole rendering | Manual stencil/mask operations | Shape with multiple contours | Native GPU-accelerated rendering |
| Winding order validation | Custom clockwise/counter-clockwise checks | Trust GeoJSON spec | GeoJSON specifies exterior CCW, interior CW |

**Key insight:** OpenRNDR's Shape class internally handles polygon triangulation and proper hole rendering using GPU-accelerated techniques. Custom implementations would be slower and more error-prone.

## Common Pitfalls

### Pitfall 1: Forgetting to Project Interior Ring Coordinates
**What goes wrong:** Holes appear in wrong screen positions or not at all
**Why it happens:** `interiorsToScreen()` is currently `TODO("Not yet implemented")`
**How to avoid:** Implement the projection transformation for all interior rings:
```kotlin
fun interiorsToScreen(projection: GeoProjection): List<List<Vector2>> {
    return interiors.map { ring ->
        ring.map { point -> Point(point.x, point.y).toScreen(projection) }
    }
}
```
**Warning signs:** Holes appear offset from expected positions, or render at geographic coordinates instead of screen coordinates.

### Pitfall 2: Not Clamping Hole Coordinates for Mercator
**What goes wrong:** Rendering artifacts when holes contain polar coordinates
**Why it happens:** Ocean/whole-world data may have coordinates beyond ±85.05112878° latitude
**How to avoid:** Apply `clampToMercator()` before projection for polygons with holes in polar regions:
```kotlin
val clampedPolygon = polygon.clampToMercator()
val exterior = clampedPolygon.exteriorToScreen(projection)
val interiors = clampedPolygon.interiorsToScreen(projection)
```
**Warning signs:** Projection overflow, infinite coordinates, or visual glitches near poles.

### Pitfall 3: Unclosed Contours in Shape Builder
**What goes wrong:** Holes don't render correctly or shape appears malformed
**Why it happens:** Forgetting to call `close()` in contour builder
**How to avoid:** Always end contour blocks with `close()` for closed polygons:
```kotlin
contour {
    moveTo(firstPoint)
    // ... lineTo calls ...
    close()  // Required for proper polygon closure
}
```
**Warning signs:** Shape renders as open path instead of filled polygon.

### Pitfall 4: Missing Update to MultiPolygon Rendering
**What goes wrong:** MultiPolygons with holes don't render holes, only exterior rings
**Why it happens:** `drawMultiPolygon()` currently only uses `polygon.exterior`
**How to avoid:** Update `drawMultiPolygon()` to check for holes and call appropriate renderer:
```kotlin
polygons.forEach { polygon ->
    if (polygon.hasHoles()) {
        drawPolygonWithHoles(drawer, polygon, projection, style)
    } else {
        drawPolygon(drawer, polygon.exteriorToScreen(projection), style)
    }
}
```
**Warning signs:** MultiPolygon holes not visible while Polygon holes work.

## Code Examples

### Complete Polygon with Holes Renderer
```kotlin
// Source: Based on OpenRNDR guide pattern
fun writePolygonWithHoles(
    drawer: Drawer,
    exterior: List<Vector2>,
    interiors: List<List<Vector2>>,
    style: Style
) {
    if (exterior.size < 3) return
    
    // Apply style
    drawer.fill = style.fill ?: ColorRGBa.WHITE.withAlpha(0.0)
    drawer.stroke = style.stroke ?: ColorRGBa.WHITE
    drawer.strokeWeight = style.strokeWeight
    drawer.lineCap = style.lineCap
    drawer.lineJoin = style.lineJoin
    
    // Build shape with exterior and holes
    val s = shape {
        // Exterior contour
        contour {
            moveTo(exterior[0])
            for (i in 1 until exterior.size) {
                lineTo(exterior[i])
            }
            close()
        }
        // Interior contours (holes)
        interiors.forEach { hole ->
            if (hole.size >= 3) {
                contour {
                    moveTo(hole[0])
                    for (i in 1 until hole.size) {
                        lineTo(hole[i])
                    }
                    close()
                }
            }
        }
    }
    
    drawer.shape(s)
}
```

### Updated drawPolygon with Hole Detection
```kotlin
fun drawPolygon(
    drawer: Drawer,
    polygon: Polygon,
    projection: GeoProjection,
    userStyle: Style? = null
) {
    val style = mergeStyles(StyleDefaults.defaultPolygonStyle, userStyle)
    
    if (polygon.hasHoles()) {
        val exterior = polygon.exteriorToScreen(projection)
        val interiors = polygon.interiorsToScreen(projection)
        writePolygonWithHoles(drawer, exterior, interiors, style)
    } else {
        val exterior = polygon.exteriorToScreen(projection)
        writePolygon(drawer, exterior, style)
    }
}
```

### Implementing interiorsToScreen
```kotlin
// In Geometry.kt - currently TODO
fun interiorsToScreen(projection: GeoProjection): List<List<Vector2>> {
    return interiors.map { ring ->
        ring.map { point ->
            Point(point.x, point.y).toScreen(projection)
        }
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| ShapeContour.fromPoints() | shape { } builder | This phase | Enables hole rendering |
| Exterior-only rendering | Full interior ring support | This phase | Correct GIS visualization |

**Deprecated/outdated:**
- `ShapeContour.fromPoints()` for polygons with holes: Use `shape { }` builder instead

## Open Questions

1. **Should we expose a separate `drawPolygonWithHoles()` public API?**
   - What we know: Current `drawPolygon()` takes `List<Vector2>` (exterior only)
   - What's unclear: Whether to add overloaded version taking `Polygon` object
   - Recommendation: Add `drawPolygon(drawer, polygon, projection, style)` overload that auto-detects holes

2. **Performance impact for complex MultiPolygons with many holes?**
   - What we know: Shape builder creates objects; may impact animation performance
   - What's unclear: Real-world performance characteristics with 100+ holes
   - Recommendation: Profile with ocean.geojson; consider caching Shape objects for static data

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4 |
| Config file | None - standard Gradle test configuration |
| Quick run command | `./gradlew test --tests "geo.render.PolygonRendererTest"` |
| Full suite command | `./gradlew test` |
| Estimated runtime | ~5 seconds |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| REND-07 | Polygon with interior rings renders correctly | unit | `./gradlew test --tests "geo.render.PolygonRendererTest.testPolygonWithHolesRendering"` | ❌ Wave 0 gap |
| REND-07 | Holes appear as transparent cutouts | unit | `./gradlew test --tests "geo.render.PolygonRendererTest.testHolesAreTransparent"` | ❌ Wave 0 gap |
| REND-08 | MultiPolygon holes clamped to Mercator bounds | unit | `./gradlew test --tests "geo.render.MultiRendererTest.testMultiPolygonWithHolesClamped"` | ❌ Wave 0 gap |
| REND-09 | Ocean data renders without artifacts | integration | Manual: Run render_BasicRendering with ocean.geojson | ❌ Manual verification |

### Nyquist Sampling Rate
- **Minimum sample interval:** After every committed task → run: `./gradlew test --tests "geo.render.*"`
- **Full suite trigger:** Before merging final task of any plan wave
- **Phase-complete gate:** Full suite green before `/gsd-verify-work` runs
- **Estimated feedback latency per task:** ~5 seconds

### Wave 0 Gaps (must be created before implementation)
- [ ] `src/test/kotlin/geo/render/PolygonRendererTest.kt` — Add `testPolygonWithHolesRendering()` test
- [ ] `src/test/kotlin/geo/render/PolygonRendererTest.kt` — Add `testHolesAreTransparent()` test  
- [ ] `src/test/kotlin/geo/render/MultiRendererTest.kt` — Add `testMultiPolygonWithHolesClamped()` test
- [ ] `src/test/kotlin/geo/GeometryTest.kt` — Add `testInteriorsToScreen()` test
- [ ] Manual verification checklist for REND-09 (ocean.geojson visual comparison)

## Sources

### Primary (HIGH confidence)
- OpenRNDR Official Guide - Curves and Shapes: https://guide.openrndr.org/drawing/curvesAndShapes.html
- OpenRNDR Shape Builder Pattern - Verified shape{contour{}} syntax for holes

### Secondary (MEDIUM confidence)
- Existing codebase analysis - PolygonRenderer.kt, Geometry.kt, MultiRenderer.kt patterns
- GeoJSON specification - Interior ring winding order conventions

### Tertiary (LOW confidence)
- None required - all patterns verified with official documentation

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - OpenRNDR Shape API is well-documented and stable
- Architecture: HIGH - Clear mapping from current to new implementation
- Pitfalls: HIGH - Based on direct code analysis and documented API behavior

**Research date:** 2026-02-27
**Valid until:** 30 days - OpenRNDR API is stable
