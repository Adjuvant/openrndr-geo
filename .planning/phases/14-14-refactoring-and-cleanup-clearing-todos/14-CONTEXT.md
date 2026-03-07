# Phase 14: Refactoring and Cleanup, Clearing TODOs - Context

**Gathered:** 2026-03-07
**Status:** Ready for planning

<domain>
## Phase Boundary

Address all accumulated TODOs, FIXMEs, and technical debt in the codebase following v1.3.0 performance work. Clean up code hygiene through systematic refactoring in a sensible order that minimizes risk while maximizing code quality improvements.

</domain>

<decisions>
## Implementation Decisions

### TODO Inventory
- **Clear ALL TODOs** from the current stack/codebase
- Includes: TODO comments, FIXMEs, deprecation warnings, and identified tech debt
- No TODOs should remain in the codebase at phase completion

### Refactoring Scope
Three categories of improvements, in order:
1. **Directory structure changes** — Reorganize packages/modules for better structure
2. **Method and code refactoring** — Extract, rename, simplify, improve readability
3. **Broken feature fixes** — Address TODOs that indicate non-working functionality

### Risk Tolerance
- **Level: Medium**
- Safe as possible approach
- Testing and building are expected to catch issues
- Interactive fixing is acceptable when tests/build fail
- No breaking API changes without careful consideration

### Completion Criteria
- **No TODOs remain** in the current codebase
- All FIXMEs addressed or documented with reasons if deferred
- Build passes
- Tests pass
- Code review standards met

### Priority/Ordering
Work sequence (sensible order):
1. **Directory structure changes** — Structural moves first (affects many files)
2. **Method and code refactoring** — Clean up logic and organization
3. **Fixes for broken features** — Address functional issues noted in TODOs
4. **AOB (Any Other Business)** — Final cleanup items, documentation updates

</decisions>

<specifics>
## Specific Ideas

- Focus on "sensible order" — structural changes before detail work
- Use build and test failures as guidance for interactive fixing
- Medium risk tolerance means some experimentation is acceptable
- Finish with AOB to capture any final cleanup not covered by TODOs

</specifics>

<deferred>
## Deferred Ideas

- None — discussion stayed within phase scope

</deferred>

---

*Phase: 14-refactoring-and-cleanup-clearing-todos*
*Context gathered: 2026-03-07*
