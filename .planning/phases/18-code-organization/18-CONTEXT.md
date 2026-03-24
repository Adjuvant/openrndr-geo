# Phase 18: Code Organization - Context

**Gathered:** 2026-03-24
**Status:** Ready for planning

<domain>
## Phase Boundary

Improve project structure through file organization and cleanup. Three sub-tasks:

1. **ORG-01:** Clean up necro examples from src/main/kotlin/geo/examples/
2. **ORG-02:** Move geo root files to core/ subdirectory
3. **ORG-03:** Organize file contents for better code navigation

This phase addresses structure only — no functional changes, no API changes.

</domain>

<decisions>
## Implementation Decisions

### ORG-01: Examples Cleanup

**Decision:** Audit the 10 files in `src/main/kotlin/geo/examples/` — merge 4 into `examples/`, delete 6 as necro/duplicates.

**Merge into examples/:**
- `layer_BlendModes.kt` → `examples/layer/` (no blend modes example exists)
- `layer_Output.kt` → `examples/layer/` (no screenshot/output example exists)
- `proj_HaversineDemo.kt` → `examples/proj/` (no haversine interpolation example exists)
- `render_LiveRendering.kt` → `examples/render/` (no olive/live-rendering example exists)

**Delete as necro/duplicate:**
- `core_CRSTransformTest.kt` — covered by `examples/proj/03-crs-transform.kt`
- `core_DataLoadingTest.kt` — covered by `examples/core/01-load-geojson.kt`, `02-load-geopackage.kt`
- `core_printSummary.kt` — covered by `examples/core/03-print-summary.kt`
- `layer_Composition.kt` — covered by `examples/layer/02-composition.kt`
- `layer_Graticule.kt` — covered by `examples/layer/01-graticule.kt`
- `proj_ProjectionTest.kt` — covered by `examples/proj/` (projection examples)

**Action:** Move 4 files to appropriate `examples/` subdirectory, delete 6 files.

---

### ORG-02: Core/ Subdirectory

**Decision:** Flat structure. All 13 geo root files move into `geo.core/` as a single package.

**Files to move (13 total):**
- `Bounds.kt`, `CachedGeoSource.kt`, `Feature.kt`, `GeoJSON.kt`, `Geometry.kt`
- `GeoPackage.kt`, `GeoSource.kt`, `GeoSourceConvenience.kt`, `GeoStack.kt`
- `loadGeo.kt`, `project.kt`, `ProjectionExtensions.kt`, `SpatialIndex.kt`

**Rationale for flat:**
- 13 files is not enough to justify subdirectories
- Flat keeps refactor surgical: one package declaration change per file (`geo` → `geo.core`), one pass updating imports
- Cross-package coupling (GeoSource depends on Feature, Geometry, Bounds, projection functions) makes subpackage boundaries artificial
- Defer structure decisions until coupling patterns emerge naturally

**No orphans:** All 13 files move — including `loadGeo.kt` and `project.kt` (entry-point status doesn't require separate package; they depend on core types and belong with them).

---

### ORG-03: Import Compatibility

**Decision:** Hard break. Update all imports across the codebase.

**Migration pattern:**
- Files importing 3+ core types: `import geo.core.*`
- Files importing 1-2 core types: explicit imports (e.g., `import geo.core.GeoSource`)

**Process:**
1. Move 13 files to `geo.core/`
2. Change package declaration in each file: `package geo` → `package geo.core`
3. Update all imports across codebase: `import geo.X` → `import geo.core.X` or `import geo.core.*`
4. Compile, fix any remaining import issues

**Precedent:** Phase 15 hard break for API reorganization — same tooling, same process.

**Single source of truth:** No shim files, no typealiases. `geo.core.GeoSource` is the only canonical path.

---

### File Grouping

**Decision:** Flat `geo.core/` holds all 13 files. No further grouping within `core/`.

**Post-move structure:**
```
src/main/kotlin/geo/
├── core/                    # Flat — all 13 files
│   ├── Bounds.kt
│   ├── CachedGeoSource.kt
│   ├── Feature.kt
│   ├── GeoJSON.kt
│   ├── Geometry.kt
│   ├── GeoPackage.kt
│   ├── GeoSource.kt
│   ├── GeoSourceConvenience.kt
│   ├── GeoStack.kt
│   ├── loadGeo.kt
│   ├── project.kt
│   ├── ProjectionExtensions.kt
│   └── SpatialIndex.kt
├── animation/
├── crs/
├── examples/               # User acceptance testing (not examples)
├── internal/
├── layer/
├── projection/
├── render/
├── tools/
└── uat/                    # User acceptance testing scripts
```

**Note:** `examples/` directory stays at `geo/examples/` (not `geo.core/examples/`). It's not part of the core library API.

</decisions>

<code_context>
## Existing Code Insights

### Current Structure
- 13 root .kt files in `src/main/kotlin/geo/`
- Established subdirs: `animation/`, `crs/`, `internal/`, `layer/`, `projection/`, `render/`, `tools/`
- `examples/` at project root — feature demos, quick-starts, elaborate demos for fun
- `uat/` at project root — user acceptance testing (not examples)

### Reusable Assets
- `examples/` subdirectories already organized by domain: `core/`, `layer/`, `proj/`, `render/`, `anim/`, `data/`, `tools/`
- `src/main/kotlin/geo/examples/` has 10 necro files to audit

### Established Patterns
- Phase 15 established tiered import structure (`geo.*`, `geo.data.*`, `geo.projection.*`, etc.)
- Phase 15 hard break precedent for API reorganization
- `internal/` subdirectory established in Phase 17 for internal utilities

### Integration Points
- Package declaration in each .kt file: `package geo` → `package geo.core`
- Import statements across entire codebase (src/main, src/test, examples/)
- Gradle build configuration may reference package paths
- Any @file:JvmName annotations

</code_context>

<specifics>
## Specific Ideas

**Import migration order:**
1. Move files first
2. Change package declarations
3. Use grep to find all `import geo\.[A-Z]` across codebase
4. Batch update imports
5. Compile to catch any missed references

**Git strategy:** Single commit for entire ORG-02 move. Easier to review and revert if needed.

</specifics>

<deferred>
## Deferred Ideas

**Structure evolution:** When `core/` grows beyond ~20 files with clear clusters (6-8 files, shared concern, minimal outward coupling), extract subpackages then. `projection/` is the likeliest candidate (already has two files and well-defined responsibility), but even that can wait.

**Façade pattern:** If a clean top-level import becomes desirable later (single file re-exporting common symbols for quick scripts), add it intentionally as a design decision — not as default placement.

</deferred>

---

*Phase: 18-code-organization*
*Context gathered: 2026-03-24*
