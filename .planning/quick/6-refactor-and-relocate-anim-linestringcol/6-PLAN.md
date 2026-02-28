---
phase: quick
plan: 6
type: execute
wave: 1
depends_on: []
files_modified:
  - src/main/kotlin/geo/examples/anim_LineStringColor.kt
  - examples/anim/06-linestring-color-anim.kt
  - examples/anim/README.md
autonomous: true
requirements:
  - REFACTOR-01
must_haves:
  truths:
    - anim_LineStringColor.kt moved from src/main/kotlin/geo/examples/ to examples/anim/
    - Refactored to use modern API patterns (geoSource, FeatureAnimator, drawer.geo)
    - Code simplified from 91 lines to ~50-60 lines
    - README.md updated with new example entry
  artifacts:
    - path: "examples/anim/06-linestring-color-anim.kt"
      provides: "Refactored LineString color animation example"
      min_lines: 50
    - path: "examples/anim/README.md"
      provides: "Updated documentation with example entry"
      contains: "06-linestring-color-anim"
  key_links:
    - from: "examples/anim/06-linestring-color-anim.kt"
      to: "examples/anim/04-stagger-animator.kt"
      via: "Modern API patterns (geoSource, FeatureAnimator, toScreen)"
---

<objective>
Refactor and relocate anim_LineStringColor example to use modern API patterns.

Purpose: Consolidate examples in the proper location and demonstrate current best practices for geographic animations.
Output: Clean, modern example in examples/anim/ with updated documentation.
</objective>

<execution_context>
@~/.config/opencode/get-shit-done/workflows/execute-plan.md
@~/.config/opencode/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@src/main/kotlin/geo/examples/anim_LineStringColor.kt
@examples/anim/04-stagger-animator.kt
@examples/anim/README.md
</context>

<tasks>

<task type="auto">
  <name>task 1: Create refactored example in examples/anim/</name>
  <files>examples/anim/06-linestring-color-anim.kt</files>
  <action>
Create new file `examples/anim/06-linestring-color-anim.kt` following the modern API pattern from `04-stagger-animator.kt`:

**Key changes from old implementation:**
1. Add `@file:JvmName("LineStringColorAnim")` annotation
2. Use `package examples.anim`
3. Replace `GeoJSON.load()` with `geoSource("examples/data/geo/catchment-topo.geojson")`
4. Use `FeatureAnimator` for per-feature animation instead of global animator
5. Use `data.features` instead of `geojson.listFeatures()`
6. Use `feature.toScreen(projection)` instead of manual coordinate mapping
7. Use `drawer.geo(source) { }` for rendering instead of `drawLineString` with manual Style
8. Simplify color interpolation logic - use `mix()` directly in render loop
9. Remove boilerplate: manual projection of each point, manual Style creation, per-geometry type handling

**Structure:**
- Load data with `geoSource()`
- Create projection with `ProjectionFactory.fitBounds()`
- Create feature animators with stagger delays for color transition
- In extend block: update animators, draw with `drawer.geo()` using animated colors
- Add text overlay for progress info

**Keep:** 
- The color interpolation between blue (#4a90d9) and red (#e74c3c)
- Property-based stroke weight mapping
- The catchment-topo.geojson data file path

**Target size:** ~50-60 lines (vs current 91)
  </action>
  <verify>
    <automated>ls examples/anim/06-linestring-color-anim.kt && head -10 examples/anim/06-linestring-color-anim.kt | grep -q "package examples.anim"</automated>
    <manual>Verify file exists and starts with correct package declaration</manual>
  </verify>
  <done>File created with modern API patterns, using geoSource, FeatureAnimator, and drawer.geo()</done>
</task>

<task type="auto">
  <name>task 2: Delete original file and update README</name>
  <files>
    - src/main/kotlin/geo/examples/anim_LineStringColor.kt
    - examples/anim/README.md
  </files>
  <action>
1. Delete the original file: `src/main/kotlin/geo/examples/anim_LineStringColor.kt`

2. Update `examples/anim/README.md`:
   - Add entry for example 06 in the examples table:
     ```
     | 06 | [06-linestring-color-anim.kt](06-linestring-color-anim.kt) | Animate LineString colors with property-based styling |
     ```
   - Add a note about property-based animation in Key Concepts section (optional, if it adds value)
   
3. Verify the example count is consistent (was 3 examples listed, now should be 4)
  </action>
  <verify>
    <automated>! test -f src/main/kotlin/geo/examples/anim_LineStringColor.kt && grep -q "06-linestring-color-anim" examples/anim/README.md</automated>
    <manual>Original file deleted and README updated with new entry</manual>
  </verify>
  <done>Original file removed, README updated with example 06 entry</done>
</task>

<task type="auto">
  <name>task 3: Verify example compiles</name>
  <files>examples/anim/06-linestring-color-anim.kt</files>
  <action>
Verify the refactored example compiles successfully:

1. Run Kotlin compiler check on the new file:
   ```bash
   ./gradlew compileKotlin
   ```

2. Check for any import errors or API mismatches.

3. Fix any compilation issues if they arise (likely related to:
   - Missing imports
   - Incorrect API method names
   - Type mismatches with FeatureAnimator usage)
  </action>
  <verify>
    <automated>./gradlew compileKotlin 2>&1 | grep -q "BUILD SUCCESSFUL"</automated>
    <manual>Compilation succeeds with no errors</manual>
  </verify>
  <done>Example compiles successfully with no errors</done>
</task>

</tasks>

<verification>
- [ ] Original file `src/main/kotlin/geo/examples/anim_LineStringColor.kt` deleted
- [ ] New file `examples/anim/06-linestring-color-anim.kt` created with modern API
- [ ] README.md updated with example 06 entry
- [ ] Code compiles successfully
- [ ] Line count reduced (target: ~50-60 lines vs original 91)
</verification>

<success_criteria>
- Example moved from src/main/kotlin/geo/examples/ to examples/anim/
- Uses modern API: geoSource(), FeatureAnimator, drawer.geo(), toScreen()
- README.md documents the new example
- Code is simpler and easier to follow
- Compiles without errors
</success_criteria>

<output>
After completion, create `.planning/quick/6-refactor-and-relocate-anim-linestringcol/6-SUMMARY.md`
</output>
