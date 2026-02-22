---
status: solved

---

# Debug Session: TemplateLiveProgram Example Script Missing

## Symptoms

**Expected:**
- A runnable example program exists at `src/main/kotlin/geo/examples/LiveRendering.kt` (or similar)
- Example demonstrates rendering features using TemplateLiveProgram.kt as a base
- Code can be modified while program is running (hot reload via oliveProgram)

**Actual:**
- The `examples` directory and files don't exist
- No example scripts demonstrating the rendering features were created

**Context:**
- Phase 03-core-rendering was supposed to create test scripts including runnable example programs
- TemplateLiveProgram.kt exists at src/main/kotlin/TemplateLiveProgram.kt as a base template
- Rendering code exists in src/main/kotlin/geo/render/ (Style, Point, LineString, Polygon, Multi* renderers)
- Test data exists in data/ directory (sample.geojson, geo/*.gpkg)

## Evidence

**Evidence 1: All phase plans reviewed**
- 03-01-PLAN.md: Created Style.kt, Shape.kt, StyleDefaults.kt, PointRenderer.kt - NO examples mentioned
- 03-02-PLAN.md: Created LineRenderer.kt, PolygonRenderer.kt, render.kt - NO examples mentioned
- 03-03-PLAN.md: Created MultiRenderer.kt, toScreen extension, docs/rendering.md - NO examples mentioned

**Evidence 2: Phase context file (03-CONTEXT.md)**
- Mentions "Creative coding ergonomics - DSL syntax aligns with OpenRNDR patterns and orx-olive live-coding workflow"
- Does NOT explicitly mention creating example scripts in any section
- Focuses on API design patterns, performance, and styling options

**Evidence 3: PROJECT.md file**
- Mentions "Live-coding support via oliveProgram — existing for hot-reloading"
- Lists requirements but doesn't explicitly require example scripts for each phase
- Active requirement: "Drawing adapters — map geo primitives to OpenRNDR Contour, Vector2, shapes"

**Evidence 4: Template files exist**
- TemplateLiveProgram.kt exists with basic oliveProgram template
- TemplateProgram.kt exists for non-live programs
- These are base templates but no derived examples exist

**Evidence 5: Rendering infrastructure complete**
- All rendering components created: Style, Shape, StyleDefaults, PointRenderer, LineRenderer, PolygonRenderer, MultiRenderer
- Documentation created at docs/rendering.md with usage examples
- Test data available in data/ directory
- BUT: No runnable examples using this infrastructure

**Evidence 6: Other testing files exist**
- TestingValidation-phase1.kt exists (for Phase 1)
- TestingValidation-phase2.kt exists (for Phase 2)
- No TestingValidation-phase3.kt exists (for Phase 3/rendering)

## Eliminated

- **hypothesis: examples directory was created but then deleted**
  - evidence: No git history of deletion found; directory never existed
  - timestamp: 2026-02-22

- **hypothesis: examples were planned in a later phase**
  - evidence: No subsequent phases mentioned in planning files (03-01, 03-02, 03-03 are all the plans for this phase)
  - timestamp: 2026-02-22

## Current Focus

**hypothesis:** Example scripts were NEVER in the plan for Phase 3 - they were an implicit expectation, not an explicit requirement

**test:** Review all plan files and summaries to confirm if examples were ever mentioned

**expecting:** If hypothesis is confirmed, the "bug" is that the plans didn't include example scripts as deliverables

**next_action:** Formulate root cause finding and what needs to be created

## Resolution

**root_cause:** Example scripts were NOT included in the phase plans. The 03-core-rendering phase (all three plans: 03-01, 03-02, 03-03) explicitly created rendering infrastructure (Style, Point/Line/Polygon/Multi renderers) and documentation, but did NOT include a task for creating runnable example programs. The expectation for examples at `src/main/kotlin/geo/examples/LiveRendering.kt` was never captured in the planning documents.

**Key findings:**
1. **No explicit requirement:** None of the three PLAN.md files (03-01, 03-02, 03-03) mention creating example scripts
2. **Documentation exists but no runnable code:** docs/rendering.md was created with code examples, but these are documentation snippets, not executable programs
3. **Template exists but no derivatives:** TemplateLiveProgram.kt exists at the root but no geo-specific examples were derived from it
4. **Pattern from previous phases:** Phase 1 and 2 had TestingValidation files, but Phase 3's TestingValidation was never created

**What needs to be created:**

1. **Directory structure:** `src/main/kotlin/geo/examples/`

2. **Example files needed:**
   - `LiveRendering.kt` - Main example demonstrating all rendering features
   - Should use `oliveProgram` for hot-reload capability
   - Should demonstrate: Style DSL, Point rendering (Circle/Square/Triangle), LineString, Polygon, Multi* geometries
   - Should use test data from `data/` directory (sample.geojson, geo/*.gpkg)
   - Should build on TemplateLiveProgram.kt pattern

3. **Specific content requirements:**
   - Import and use rendering functions from geo.render package
   - Demonstrate Style { } DSL syntax
   - Show different shapes: Shape.Circle, Shape.Square, Shape.Triangle
   - Show LineString with different caps/joins
   - Show Polygon with fill/stroke/opacity
   - Use projection (from Phase 2) to convert geo coordinates to screen
   - Load and render actual GeoJSON data

**files_changed:**
- NEW: src/main/kotlin/geo/examples/LiveRendering.kt (to be created)
- NEW: src/main/kotlin/geo/examples/ (directory to be created)
