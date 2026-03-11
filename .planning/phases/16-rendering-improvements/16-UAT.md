---
status: testing
phase: 16-rendering-improvements
source: [16-04-UAT.md, 16-VERIFICATION.md]
started: 2026-03-11T12:00:00Z
updated: 2026-03-11T12:00:00Z
---

# UAT for Phase 16: Rendering Improvements Gap Closure

The following User Acceptance Tests validate the rendering fixes for antimeridian crossing, polygon holes, and viewport cache hole preservation. Automated builds have completed successfully; visual verification is required to confirm correct behavior.

## Tests

### 1. Polygon Bug Fixing
- File: `uat/polygon-bug-fixing.kt`
- Purpose: Verify antimeridian crossing is correctly split and polygon holes render as cutouts when using `drawer.geo()`.
- How to run: `./gradlew run -Popenrndr.application=uat.PolygonBugFixingJvm`
- Result: **BUILD SUCCESSFUL**
- Notes: Visual inspection required to confirm that:
  - Polygons crossing the ±180° meridian are split and rendered seamlessly.
  - Polygons with holes display transparent cutouts at the correct locations.

### 2. Viewport Cache Holes
- File: `uat/viewport-cache-holes.kt`
- Purpose: Verify viewport caching preserves interior ring (hole) coordinates and renders transparent cutouts using compound boolean operations.
- How to run: `./gradlew run -Popenrndr.application=uat.ViewportCacheHolesKt`
- Result: **BUILD SUCCESSFUL**
- Notes: Visual inspection required to confirm that:
  - Cached projected coordinates include interior rings.
  - Polygons render with transparent holes (no filled patches where holes exist).

## Summary
- Automated build and application startup for both tests succeeded.
- Visual UAT is pending to confirm actual rendering behavior.

## Next Steps
1. Perform visual verification of both test applications.
2. If rendering issues are observed, diagnose root causes and update fix plans accordingly.
3. If visual UAT passes, update phase status to **passed** and close remaining gaps.
