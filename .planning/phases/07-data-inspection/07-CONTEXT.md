# Phase 7: Data Inspection - Context

**Gathered:** 2026-02-26
**Status:** Ready for planning

<domain>
## Phase Boundary

Users can call `printSummary()` on any GeoSource to understand their geo data before rendering. This is a diagnostic/inspection utility for debugging and data exploration.

**What's included:**
- Feature count
- Bounds and CRS
- Geometry type distribution (e.g., "3 Point, 2 LineString, 5 Polygon")
- Memory footprint estimate
- Property keys + types

**What's NOT included:**
- Property sample values (user can inspect these themselves after seeing keys/types)
- Programmatic access to summary data (console output only)
- Multiple detail modes (single printSummary method)

</domain>

<decisions>
## Implementation Decisions

### Output format
- Print to console only — no programmatic return value (Unit return)
- Pandas DataFrame.summary() style — well-structured, readable format
- Output goes to stdout via `println()` (project doesn't use logging framework)

### Property inspection
- Show property **keys + types only**
- Do NOT include sample values (user can query these themselves)
- No need for property value statistics or distributions

### Performance modes
- Single `printSummary()` method — no quick/detailed modes
- Method must iterate through features once to collect geometry types and bounds
- Memory estimate based on feature count × average feature size

### API style
- Instance method on GeoSource: `source.printSummary()`
- Method name makes side effect clear (prints to console)
- Matches existing pattern: `source.render()` also has Unit return

### OpenCode's Discretion
- Output layout and visual formatting — "make it clean"
- How to format geometry type distribution (counts vs percentages vs both)
- Memory estimation formula (rough approximation acceptable)
- Section ordering and visual separators

</decisions>

<specifics>
## Specific Ideas

- Inspired by pandas DataFrame.summary() — structured, scannable output
- "Clean af" — prioritize readability and visual clarity
- Should feel like a standard debugging/diagnostic tool

</specifics>

<deferred>
## Deferred Ideas

- Property sample values in summary — out of scope (user can query separately)
- Return summary as data class for programmatic access — different use case
- Multiple verbosity levels (quick vs detailed) — not needed for v1.2.0

</deferred>

---

*Phase: 07-data-inspection*
*Context gathered: 2026-02-26*
