---
phase: quick
plan: 8
type: execute
wave: 1
depends_on: []
files_modified:
  - examples/render/07-geostack-render.kt
autonomous: true
requirements:
  - Q8-01

must_haves:
  truths:
    - "Example loads multiple GeoJSON datasets"
    - "Example creates a GeoStack combining all sources"
    - "Example renders the unified stack to screen with auto-fitting projection"
    - "Example demonstrates CRS unification in visual context"
  artifacts:
    - path: "examples/render/07-geostack-render.kt"
      provides: "Complete GeoStack rendering example"
      min_lines: 50
      exports: ["main"]
    - path: "examples/render/README.md"
      provides: "Updated index with new example"
  key_links:
    - from: "07-geostack-render.kt"
      to: "geoStack() function"
      pattern: "geoStack("
    - from: "07-geostack-render.kt"
      to: "GeoStack.render()"
      pattern: "stack.render("
---

<objective>
Create example 07-geostack-render.kt in examples/render/ that demonstrates rendering multiple geographic datasets through a unified GeoStack with automatic CRS unification.

Purpose: Bridge the gap between the console-only GeoStack example and real-world visualization needs
Output: Complete runnable example with README update
</objective>

<execution_context>
@~/.config/opencode/get-shit-done/workflows/execute-plan.md
</execution_context>

<context>
@examples/core/04-geostack.kt (Reference for GeoStack usage)
@examples/render/06-quick-geo.kt (Reference for render example structure)
@examples/render/01-points.kt (Reference for detailed render patterns)
@src/main/kotlin/geo/GeoStack.kt (Feature being demonstrated)
</context>

<tasks>

<task type="auto">
  <name>Create GeoStack rendering example</name>
  <files>examples/render/07-geostack-render.kt</files>
  <action>
Create a complete GeoStack rendering example that demonstrates multi-dataset composition with visual output.

Follow the established example pattern:
1. Use `@file:JvmName("GeoStackRender")` annotation
2. Package: `examples.render`
3. Window size: 800x600 (consistent with other render examples)
4. Load 3 datasets: sample.geojson (polygons), rivers_lakes.geojson (lines), populated_places.geojson (points)
5. Create a geoStack() combining all three sources
6. Use stack.render(drawer) for automatic projection fitting
7. Set white background with drawer.clear(ColorRGBa.WHITE)

Include comprehensive header comment:
- Title: "## 07 - GeoStack Rendering"
- Description of what it demonstrates
- Concepts section listing: geoStack(), automatic CRS unification, multi-dataset overlay rendering, auto-fit projection
- To Run command: `./gradlew run -Popenrndr.application=examples.render.GeoStackRenderKt`

The example should be clean and simple - no custom styling needed, just show the unified rendering capability.
  </action>
  <verify>
    <automated>cat examples/render/07-geostack-render.kt | grep -E "(geoStack|stack\.render|@file:JvmName|package examples\.render)" | wc -l | test "$(cat)" -ge 4</automated>
    <manual>Verify file exists and has proper structure with header comment</manual>
  </verify>
  <done>File exists with proper package, JvmName annotation, three datasets loaded, geoStack created, and render call in extend block</done>
</task>

<task type="auto">
  <name>Update README with new example</name>
  <files>examples/render/README.md</files>
  <action>
Update the examples table in examples/render/README.md to include the new example:

Add row to the table:
`| 06-quick-geo.kt | Quick geo rendering | populated_places.geojson |`
`| 07-geostack-render.kt | GeoStack multi-dataset rendering | sample.geojson, rivers_lakes.geojson, populated_places.geojson |`

Update the Key Concepts section to add:
- `geoStack()` for multi-dataset composition
- `GeoStack.render()` for unified rendering with auto-fit

Ensure the table maintains proper markdown formatting.
  </action>
  <verify>
    <automated>grep -q "07-geostack-render" examples/render/README.md && grep -q "geoStack" examples/render/README.md</automated>
  </verify>
  <done>README.md updated with new example entry and key concepts</done>
</task>

</tasks>

<verification>
- [ ] Example file compiles without errors
- [ ] Example follows naming convention (numbered file with JvmName)
- [ ] Example demonstrates all required concepts (multi-dataset, GeoStack, rendering)
- [ ] README updated with new entry
</verification>

<success_criteria>
- examples/render/07-geostack-render.kt exists and is runnable
- Example loads 3+ datasets and creates a GeoStack
- Example renders unified view with stack.render(drawer)
- README.md updated with new example entry
</success_criteria>

<output>
After completion, create `.planning/quick/8-create-geostack-rendering-example-in-exa/8-SUMMARY.md`
</output>
