---
status: investigating
trigger: "TemplateProgram Example Script is missing - expected src/main/kotlin/geo/examples/BasicRendering.kt (or similar) demonstrating rendering features"
created: 2026-02-22T00:00:00Z
updated: 2026-02-22T14:50:00Z
---

## Current Focus

hypothesis: The example scripts were never planned in Phase 03-core-rendering PLANS
investigating: Review PLAN files (03-01, 03-02, 03-03) for example script requirements
test: Check each PLAN for tasks creating example programs
expecting: Find if example creation was explicitly planned or is a gap
next_action: Compile findings and determine root cause

## Symptoms

expected: A runnable example program exists at src/main/kotlin/geo/examples/BasicRendering.kt (or similar) that demonstrates rendering features using TemplateProgram.kt as a base
actual: The examples directory and files don't exist
created_when: Phase 03-core-rendering was supposed to be complete
context: |
  - Phase 03-core-rendering was supposed to create test scripts including runnable example programs
  - TemplateProgram.kt exists at src/main/kotlin/TemplateProgram.kt as a base template
  - Rendering code exists in src/main/kotlin/geo/render/ (Style, Point, LineString, Polygon, Multi* renderers)
  - Test data exists in data/ directory (sample.geojson, geo/*.gpkg)

## Eliminated

## Evidence

- timestamp: 2026-02-22T14:45:00Z
  checked: PLAN 03-01-PLAN.md
  found: Tasks 1-3 create Style, StyleDefaults, PointRenderer - NO example programs mentioned
  implication: Example scripts were not in scope for 03-01

- timestamp: 2026-02-22T14:46:00Z
  checked: PLAN 03-02-PLAN.md
  found: Tasks 1-3 create LineRenderer, PolygonRenderer, render.kt - NO example programs mentioned
  implication: Example scripts were not in scope for 03-02

- timestamp: 2026-02-22T14:47:00Z
  checked: PLAN 03-03-PLAN.md
  found: Task 3 creates "documentation with usage examples" - but specifies docs/rendering.md NOT runnable example programs
  implication: Documentation was planned, but not executable example programs

- timestamp: 2026-02-22T14:48:00Z
  checked: docs/rendering.md
  found: Complete documentation exists with code examples, including complete example program at end (lines 348-425)
  implication: Documentation exists but is not a runnable program file

- timestamp: 2026-02-22T14:49:00Z
  checked: src/main/kotlin/geo/examples/ directory
  found: Directory does not exist - no files found via glob pattern
  implication: Examples directory was never created

- timestamp: 2026-02-22T14:50:00Z
  checked: 03-UAT.md test cases 2 and 3
  found: |
    Test 2 expected: "A runnable example program exists at src/main/kotlin/geo/examples/BasicRendering.kt"
    Test 2 result: issue - "files don't exist"
    Test 3 expected: "A runnable example program exists at src/main/kotlin/geo/examples/LiveRendering.kt"
    Test 3 result: issue - "same - files don't exist"
  implication: UAT expected these files, but they were not delivered by any PLAN

## Resolution

root_cause: Example scripts were NEVER explicitly planned in any of the three Phase 03-core-rendering PLAN files. The UAT tests 2 and 3 expected runnable example programs at src/main/kotlin/geo/examples/BasicRendering.kt and LiveRendering.kt, but no plan (03-01, 03-02, or 03-03) included tasks to create them. Plan 03-03 Task 3 was documented as "Create documentation with usage examples" but specifically targeted docs/rendering.md, not runnable programs. The documentation exists and contains complete code examples, but they are not standalone runnable example files.

fix: Create the two missing example files as expected by the UAT:
1. src/main/kotlin/geo/examples/BasicRendering.kt - using TemplateProgram.kt structure
2. src/main/kotlin/geo/examples/LiveRendering.kt - using TemplateLiveProgram structure (hot reload capable)

verification: After creation, both files should:
- Compile successfully with ./gradlew build
- Be executable as standalone OpenRNDR programs
- Demonstrate all rendering features (Style, Point, LineString, Polygon, Multi* geometries)
- Use the test data from data/ directory

files_changed: []
