# Phase 10: Documentation & Examples - Context

**Gathered:** 2026-02-27
**Status:** Ready for planning

<domain>
## Phase Boundary

Create runnable examples with sample data for users to learn the library. Examples are organized by category, include data files, and demonstrate single concepts. Examples also serve as a living reference for the project owner to identify feature gaps and plan improvements.

</domain>

<decisions>
## Implementation Decisions

### Example Structure & Naming
- Category folders with numbered files: `examples/render/01-polygon.kt`
- Numbered descriptive names: `01-xxx.kt`, `02-xxx.kt`
- Two tiers: minimal examples for single concepts, full-workflow examples for complete flows
- Categories mirror source packages: `core_`, `render_`, `proj_`, `anim_`, `layer_`
- Current examples folder may contain outdated code — replace as needed, no need to preserve

### Sample Data Files
- Mix of synthetic/hand-crafted and real-world data
- Size varies by example complexity (small for basics, larger for advanced)
- Stored in `examples/data/geo/` (geo subdirectory for future extensibility to fonts, images, CSV/JSON with geo fields)
- Formats: GeoJSON + GeoPackage
- **Note:** Researcher/planner should flag any data files that require manual download or preparation

### Documentation Format
- KDoc comments in each example (IDE hover support)
- README.md per category folder (overview, list of examples, concepts covered)
- Show just the essential code — minimal boilerplate, focus on key API calls
- Standalone examples — no external links to API docs

### How to Run Examples
- Individual `main()` functions per example
- Run via `./gradlew run --args="render/01-polygon"` style
- Visual output in OPENRNDR window (user sees rendered result)
- Zero config required — just Gradle
- No batch "run all" task needed (run individually only)

### Project Owner Notes
- Examples must link to current API level (not outdated patterns)
- WIP/incomplete features should be noted in examples
- Examples serve as tool for owner to plan new features and identify shortcomings

### OpenCode's Discretion
- Exact naming of categories if source packages differ
- How to handle examples that span multiple categories
- Console output format for non-visual examples
- Whether to include a root examples/README.md

</decisions>

<specifics>
## Specific Ideas

- "Current examples may contain necro code — replace if out of date"
- "Examples provide me, the project owner, the ability to plan new features and shortcomings"
- Data folder structure allows future extension to other formats (CSV, JSON with geo fields, fonts, images)

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 10-documentation-examples*
*Context gathered: 2026-02-27*
