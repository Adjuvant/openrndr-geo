---
phase: quick
plan: 7
type: execute
wave: 1
depends_on: []
files_modified:
  - examples/core/04-geostack.kt
  - examples/core/README.md
autonomous: true
requirements:
  - Create GeoStack example demonstrating multi-dataset loading
must_haves:
  truths:
    - Example loads multiple GeoJSON datasets
    - Example creates a GeoStack combining the sources
    - Example demonstrates GeoStack capabilities (source count, total features, bounding box)
  artifacts:
    - path: "examples/core/04-geostack.kt"
      provides: "Runnable GeoStack demonstration"
    - path: "examples/core/README.md"
      provides: "Updated example documentation"
  key_links:
    - from: "04-geostack.kt"
      to: "geoStack()"
      via: "import and function call"
    - from: "04-geostack.kt"
      to: "sample.geojson, populated_places.geojson"
      via: "file loading"
---

<objective>
Create a core example demonstrating GeoStack for multi-dataset composition and inspection.

Purpose: Show users how to combine multiple geographic datasets into a unified stack with automatic CRS handling.
Output: `examples/core/04-geostack.kt` and updated README.md
</objective>

<execution_context>
@~/.config/opencode/get-shit-done/workflows/execute-plan.md
@~/.config/opencode/get-shit-done/templates/summary.md
</execution_context>

<context>
@src/main/kotlin/geo/GeoStack.kt
@examples/core/01-load-geojson.kt
@examples/core/README.md
</context>

<tasks>

<task type="auto">
  <name>Create 04-geostack.kt example</name>
  <files>examples/core/04-geostack.kt</files>
  <action>
Create a new core example file `examples/core/04-geostack.kt` that demonstrates GeoStack multi-dataset composition.

**Follow the pattern from 01-load-geojson.kt:**
1. Use `@file:JvmName("GeoStack")` annotation
2. Package: `examples.core`
3. KDoc header with title, description, concepts, and run instructions
4. Main function that loads multiple datasets and creates a GeoStack

**Example content:**
- Load at least 2-3 GeoJSON files (use sample.geojson, populated_places.geojson, rivers_lakes.geojson)
- Create a GeoStack using `geoStack(source1, source2, ...)`
- Demonstrate GeoStack capabilities:
  - `sourceCount()` - show number of sources
  - `crs` - show unified CRS
  - `features.toList().size` - show total feature count
  - `totalBoundingBox()` - show combined bounds
- Print formatted output to console similar to 03-print-summary.kt style

**Do NOT include rendering** - this is a core example (console output only).

**Run instructions in KDoc:**
```
./gradlew run -Popenrndr.application=examples.core.GeoStackKt
```
  </action>
  <verify>
    <automated>cat examples/core/04-geostack.kt | grep -q "geoStack" && cat examples/core/04-geostack.kt | grep -q "@file:JvmName" && echo "Example file created with correct structure"</automated>
    <manual>Verify file follows existing example patterns (KDoc, imports, main function)</manual>
  </verify>
  <done>04-geostack.kt exists with proper structure, loads multiple datasets, creates GeoStack, and prints summary</done>
</task>

<task type="auto">
  <name>Update examples/core/README.md</name>
  <files>examples/core/README.md</files>
  <action>
Update the examples/core/README.md to include the new GeoStack example.

**Changes to make:**
1. Add entry to the examples table:
   | 04-geostack.kt | Multi-dataset composition | sample.geojson, populated_places.geojson, rivers_lakes.geojson |

2. Add to Key Concepts section:
   - `geoStack()` for combining multiple GeoSources
   - Automatic CRS unification across sources
   - `totalBoundingBox()` for combined bounds

Follow the existing README format exactly.
  </action>
  <verify>
    <automated>cat examples/core/README.md | grep -q "04-geostack" && echo "README updated with GeoStack example"</automated>
    <manual>Verify table formatting matches existing entries</manual>
  </verify>
  <done>README.md includes 04-geostack.kt in examples table and GeoStack concepts in key concepts</done>
</task>

</tasks>

<verification>
- File examples/core/04-geostack.kt exists and compiles
- File uses @file:JvmName annotation for proper class naming
- File loads multiple GeoJSON sources and creates GeoStack
- Console output shows GeoStack capabilities (source count, features, bounds)
- README.md is updated with new example entry
</verification>

<success_criteria>
- Single focused example file created in examples/core/
- Example demonstrates loading multiple datasets into GeoStack
- Example shows GeoStack inspection capabilities (no rendering)
- README documentation updated
- Example follows established patterns from existing core examples
</success_criteria>

<output>
After completion, create `.planning/quick/7-create-geostack-example-for-multi-datase/7-SUMMARY.md`
</output>
