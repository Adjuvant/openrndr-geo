# Phase 8: Rendering Improvements - Context

**Gathered:** 2026-02-26
**Status:** Ready for planning

<domain>
## Phase Boundary

Users can render complex polygons (with holes) correctly using OpenRNDR's Shape API. This phase focuses on:
- Polygon features with interior rings (holes)
- MultiPolygon features with interior rings
- Mercator bounds clamping for holes
- Verification through sample data

</domain>

<decisions>
## Implementation Decisions

### Implementation approach
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

</decisions>

<specifics>
## Specific Ideas

- OpenRNDR shape builder pattern:
  ```kotlin
  val s = shape {
      contour { moveTo(...); lineTo(cursor + Vector2(...)); ... }
      contour { moveTo(...); lineTo(cursor + Vector2(...)); ... } // hole
  }
  ```
- Relative moves between points need transformation from absolute geo coordinates
- Standard GIS behavior: holes are transparent cutouts in polygon fill

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 08-rendering-improvements*
*Context gathered: 2026-02-26*
