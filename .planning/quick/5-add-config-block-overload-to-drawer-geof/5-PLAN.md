---
phase: 5-add-config-block-overload-to-drawer-geof
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - src/main/kotlin/geo/render/DrawerGeoExtensions.kt
autonomous: true
requirements: []
must_haves:
  truths:
    - "Drawer.geoFeatures has config block overload"
    - "Config overload supports styleByType and styleByFeature"
    - "Auto-fit projection works when no projection specified"
    - "Existing basic overload remains functional"
  artifacts:
    - path: "src/main/kotlin/geo/render/DrawerGeoExtensions.kt"
      provides: "Config block overload for geoFeatures"
      contains: "fun Drawer.geoFeatures(features: Sequence<Feature>, block: GeoRenderConfig.() -> Unit)"
  key_links:
    - from: "geoFeatures overload"
      to: "GeoRenderConfig"
      via: "config block parameter"
    - from: "geoFeatures"
      to: "resolveStyle"
      via: "style resolution"
---

<objective>
Add config block overload to Drawer.geoFeatures following the same pattern as Drawer.geo.

Purpose: Complete the two-tier API for feature sequences, enabling per-feature and by-type styling in addition to basic rendering.
Output: New overload function with GeoRenderConfig support
</objective>

<execution_context>
@~/.config/opencode/get-shit-done/workflows/execute-plan.md
@~/.config/opencode/get-shit-done/templates/summary.md
</execution_context>

<context>
@src/main/kotlin/geo/render/DrawerGeoExtensions.kt
@src/main/kotlin/geo/render/GeoRenderConfig.kt
</context>

<tasks>

<task type="auto">
  <name>Add config block overload to Drawer.geoFeatures</name>
  <files>src/main/kotlin/geo/render/DrawerGeoExtensions.kt</files>
  <action>
Add a new overload of `Drawer.geoFeatures` right after the existing one (around line 159), following the exact pattern used by `Drawer.geo` (lines 187-207).

The new function signature should be:
```kotlin
fun Drawer.geoFeatures(
    features: Sequence<Feature>,
    block: (GeoRenderConfig.() -> Unit)? = null
)
```

Implementation steps:
1. Create GeoRenderConfig from block (or default)
2. Calculate bounds from all features (reuse existing logic from basic overload)
3. Create auto-fit projection if not specified in config
4. Snapshot the config for safe iteration
5. Iterate features, resolve style using resolveStyle(), render each geometry

Key implementation details to match existing code:
- Use `features.toList()` and `fold(Bounds.empty())` for bounds calculation
- Use `ProjectionFactory.fitBounds()` with same parameters (padding=0.0 for features)
- Call `snapshot()` before iteration for thread safety
- Use `resolveStyle(feature, resolved)` for style resolution
- Call `feature.geometry.renderToDrawer(this, proj, style)` for rendering

Add KDoc documentation matching the style of Drawer.geo() with Tier 1 (one-liner) and Tier 2 (config block) examples.
  </action>
  <verify>
    <automated>./gradlew compileKotlin --quiet</automated>
    <manual>Verify the function exists and compiles without errors</manual>
  </verify>
  <done>New overload compiles, existing tests pass, follows Drawer.geo pattern</done>
</task>

</tasks>

<verification>
- [ ] New overload compiles successfully
- [ ] Existing tests continue to pass
- [ ] Code follows established patterns (Drawer.geo)
- [ ] KDoc documentation present
</verification>

<success_criteria>
- Drawer.geoFeatures() accepts optional config block parameter
- Config supports projection, style, styleByType, styleByFeature
- Auto-fit projection when projection not specified
- Feature-level style resolution works correctly
</success_criteria>

<output>
After completion, create `.planning/quick/5-add-config-block-overload-to-drawer-geof/5-01-SUMMARY.md`
</output>
