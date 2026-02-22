# Phase 3: Core Rendering Context

## Goal

Users can visualize geo primitives with configurable styling across Point, LineString, Polygon geometries with a consistent styling API.

## Philosophy

Performance-first for real-time animation with zero-allocation principles. The API prioritizes mutable state for kinetic visual effects (points traveling along borders, frame-by-frame updates) while maintaining smooth framerates by minimizing garbage collection overhead.

Creative coding ergonomics - DSL syntax aligns with OpenRNDR patterns and orx-olive live-coding workflow.

## Key Areas

### 1. Styling API Approach

**Q1: Which syntax for creative coders?**
✓ **DSL syntax** with invoke() operator
- Example: `drawFeature { color = Color.BLUE; strokeWeight = 2.0 }`
- Rationale: Consistent with Phase 2 projection patterns (`ProjectionMercator { width = 800 }`)
- Preserves orx-olive live-coding compatibility

**Q2: How to create style objects?**
✓ **DSL syntax** with invoke() operator
- Consistent pattern across the library
- Example: `Style { color = Color.RED; strokeWeight = 3.0 }`

**Q3: Reusable styles?**
✓ **Optional reusability**
- Users can define `defaultStyle` once: `val defaultStyle = Style { color = Color.RED }`
- Or per-feature: `forEach { it.style = Style { color = Color.BLUE } }`
- Flexibility for different workflows

**Q4: Mutable or immutable?**
✓ **Mutable objects** (standard object mutation)
- Example: `style.color = Color.RED; style.strokeWeight = 3.0`
- **Performance context:** Zero-allocation for smooth framerates
- **Animation context:** Real-time kinetic animation requires frequent updates to visual properties
- Mutable state works better with hardware-accelerated OpenRNDR environment

---

### 2. Point Rendering Options

**Q1: What basic shapes?**
✓ **Comprehensive:** circles, squares, triangles + shape enums
- Shape enum: `Shape.Circle, Shape.Square, Shape.Triangle`
- Provides primitive variety for different visualization needs

**Q2: Custom markers?**
✓ **No for v1, add to v2 backlog**
- V2 todo: Support SVGs/images as custom markers
- V2 note: Could explore texture-based markers for performance

**Q3: Size control?**
✓ **Both absolute pixels and data-driven**
- Absolute: `size = 10.0` - 10x10 circle
- Data-driven: `size = feature.properties["magnitude"] as Double`
- Lambda support: `size = { it.properties["mag"] as Double * 2.0 }`

**Q4: Default size?**
✓ **Fixed reasonable default:** 5.0 pixels
- 5x5 circle, 5x5 square by default
- Users can override per-feature

---

### 3. Line/Polygon Features

**Q1: Dashed lines support?**
✓ **Yes - configurable dash patterns**
- Example: `dashPattern = floatArrayOf(10f, 5f)` - 10px draw, 5px gap
- Flexible pattern specification for any visualization style

**Q2: Stroke caps/joins?**
✓ **Full OpenRNDR standard**
- Caps: butt, round, square
- Joins: round, miter, bevel
- Exposes professional-grade rendering capabilities

**Q3: Fill opacity?**
✓ **Alpha channel in ColorRGBa**
- Example: `fill = ColorRGBa.RED.withAlpha(0.5)`
- Uses OpenRNDR's native ColorRGBa type
- No separate opacity property needed

**Q4: Hatching patterns?**
✓ **Optional - basic hatch patterns if easy to implement**
- Only if straightforward to add
- Defer complex patterns to v2

**Note:** Do not duplicate functionality already provided by OpenRNDR. Build on top of Contour, Shape, ColorRGBa, etc.

---

### 4. Style Defaults

**Q1: Sensible defaults for geometry types?**
✓ **Yes** - minimal defaults (not duplicating OpenRNDR)
- `drawPoint()`: `size = 5.0`, `color = Color.WHITE`, `shape = Shape.Circle`
- `drawLine()`: `stroke = Color.WHITE`, `strokeWeight = 1.0`
- `drawPolygon()`: `stroke = Color.WHITE`, `fill = Color.WHITE.transparent()`
- Users can override all defaults

**Q2: Override mechanism?**
✓ **System defaults + per-geometry merge**
- System provides internal defaults (`defaultPointStyle`, `defaultLineStyle`, etc.)
- Users can call directly: `drawPoint(point)` → uses `defaultPointStyle`
- Users can override: `drawPoint(point, style = Style(size = 10.0))` → merges with user style winning on conflicts

**Q3: CRS-aware defaults?**
✓ **No** - CRS is data origin, visualization decisions belong to user
- WGS84 points and BNG points should not be differentiated by the library
- User decides representation (both colors red, or different colors based on their criteria)

**Q4: Conflict resolution?**
✓ **Merge with override precedence**
- System default provides base values
- Per-geometry style merges with default
- Per-geometry takes precedence on conflicts (e.g., user overrides color, but keeps default size)

---

## Implementation Notes

**Performance constraints:**
- Zero-allocation principle for real-time animation framerates
- Mutable state preferred over immutable copies
- Avoid object allocation per frame

**OpenRNDR integration:**
- Use ColorRGBa for all color operations
- Leverage OpenRNDR's Contour class for stroking/filling
- Shape system builds on OpenRNDR shapes extension

**API boundaries:**
- Don't duplicate existing OpenRNDR functionality
- Build conveniences on top, don't reinvent
- Expose OpenRNDR capabilities through DSL syntax

---

## Previous Phase Patterns

**Phase 2 patterns to build on:**
- DSL syntax with invoke() operator (e.g., `ProjectionMercator { width = 800 }`)
- Dual API style (procedural functions + extension methods)
- Convenience-first design with clear structure

---

## V2 Roadmap Ideas

- Custom markers (SVGs, images, textures)
- Advanced hatching patterns
- Shape-based point clustering
- GPU acceleration for large datasets