---
phase: 10-fix-viewport-cache-bypass-in-drawer-geo-
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - src/main/kotlin/geo/render/DrawerGeoExtensions.kt
autonomous: true
requirements:
  - PERF-04
  - PERF-07
must_haves:
  truths:
    - Drawer.geo() uses viewport cache for coordinate projection
    - Cache is cleared on viewport change
    - Geometry dirty flag invalidates cache entries
  artifacts:
    - path: src/main/kotlin/geo/render/DrawerGeoExtensions.kt
      provides: "Viewport cache integration in Drawer.geo()"
      min_lines: 10
  key_links:
    - from: Drawer.geo()
      to: ViewportCache.getProjectedCoordinates()
      via: "function call with ViewportState"
      pattern: "viewportCache\\.getProjectedCoordinates"
---

<objective>
Fix the viewport cache bypass in Drawer.geo() extension function. Currently it calls geometry.renderToDrawer() which projects coordinates on every frame, completely bypassing the ViewportCache mechanism. The fix integrates ViewportCache usage like GeoStack.render() does.

Purpose: Ensure single-geometry rendering uses the same performance optimization as multi-source rendering through GeoStack.
Output: Updated DrawerGeoExtensions.kt with viewport cache integration
</objective>

<execution_context>
@~/.config/opencode/get-shit-done/workflows/execute-plan.md
</execution_context>

<context>
@src/main/kotlin/geo/render/DrawerGeoExtensions.kt
@src/main/kotlin/geo/GeoStack.kt
@src/main/kotlin/geo/internal/cache/ViewportCache.kt
@src/main/kotlin/geo/internal/cache/ViewportState.kt
</context>

<tasks>

<task type="auto">
  <name>task 1: Refactor Drawer.geo() to use ViewportCache</name>
  <files>src/main/kotlin/geo/render/DrawerGeoExtensions.kt</files>
  <action>
    Modify the Drawer.geo() extension function to use viewport caching:

    1. Add a ViewportCache instance (similar to how GeoStack has one)
    2. Create ViewportState from projection using ViewportState.fromProjection()
    3. Replace the direct call to geometry.renderToDrawer() with:
       - Get projected coordinates from cache: viewportCache.getProjectedCoordinates()
       - Call a new renderProjectedCoordinates() helper that renders from cached Array<Vector2>
    
    Reference implementation from GeoStack.kt:
    - Line 72: private val viewportCache = geo.internal.cache.ViewportCache()
    - Line 222: val viewportState = geo.internal.cache.ViewportState.fromProjection(projection)
    - Lines 246-262: renderWithCache() method showing the pattern
    - Lines 268-278: projectGeometryToArray() for the projector lambda
    - Lines 338-375: renderProjectedCoordinates() rendering from cached coordinates
    
    Ensure imports are correct:
    - geo.internal.cache.ViewportCache
    - geo.internal.cache.ViewportState
    - org.openrndr.math.Vector2 (for Array<Vector2>)
    
    The renderToDrawer() method (lines 294-327) can remain for non-cached scenarios, but geo() should use the cached path.
  </action>
  <verify>
    <automated>./gradlew test --tests "*DrawerGeo*" -x 2>&1 | grep -E "(BUILD|FAILED|SUCCESS)"</automated>
    <manual>Verify DrawerGeoExtensions.kt compiles and existing tests pass</manual>
  </verify>
  <done>Drawer.geo() calls viewportCache.getProjectedCoordinates() before rendering, matching the pattern used in GeoStack.render()</done>
</task>

<task type="auto">
  <name>task 2: Add test to verify viewport cache is used</name>
  <files>src/test/kotlin/geo/render/DrawerGeoExtensionsTest.kt</files>
  <action>
    Create a test that verifies Drawer.geo() uses viewport caching:

    1. Create a test file at src/test/kotlin/geo/render/DrawerGeoExtensionsTest.kt
    2. Write a test that:
       - Creates a mock or test Drawer (may need to use a stub)
       - Creates a simple geometry (Point or LineString)
       - Calls drawer.geo(geometry) twice with the same projection
       - Verifies that the second call is faster (or check cache metrics if available)
       
    Alternative simpler test:
    - Verify the ViewportCache import and usage exists in DrawerGeoExtensions.kt
    - Verify that the geometry.renderToDrawer() call is replaced or bypassed when using geo()
    
    If Drawer mocking is difficult, at minimum verify:
    - The code compiles
    - ViewportCache is instantiated in DrawerGeoExtensions
    - ViewportState.fromProjection() is called
  </action>
  <verify>
    <automated>./gradlew test --tests "*DrawerGeoExtensionsTest*" 2>&1 | grep -E "(BUILD|tests? completed|PASSED|FAILED)"</automated>
  </verify>
  <done>Test exists and passes, confirming viewport cache integration</done>
</task>

</tasks>

<verification>
1. Code compiles without errors: `./gradlew compileKotlin`
2. All existing tests pass: `./gradlew test`
3. Drawer.geo() now uses ViewportCache pattern matching GeoStack.render()
</verification>

<success_criteria>
- Drawer.geo() extension uses ViewportCache for coordinate projection
- Cache is properly cleared on viewport state changes
- Geometry dirty flag properly invalidates individual cache entries
- All existing tests continue to pass
- Performance improvement visible in benchmarks (optional verification)
</success_criteria>

<output>
After completion, create `.planning/quick/10-fix-viewport-cache-bypass-in-drawer-geo-/10-SUMMARY.md`
</output>
