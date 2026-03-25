# Roadmap: openrndr-geo

## Milestones

- ✅ **v1.4.0 Developer Experience** — Phases 15-20 (shipped 2026-03-25) — [Details](milestones/v1.4.0-ROADMAP.md)
- ✅ **v1.3.0 Performance** — Phases 11-14 (shipped 2026-03-07) — [Details](milestones/v1.3.0-ROADMAP.md)

## Current Focus

**Milestone:** v1.5.0 (next)  
**Status:** Planning

---

## Archived Milestones

<details>
<summary>✅ v1.4.0 Developer Experience (Phases 15-20) — SHIPPED 2026-03-25</summary>

**Achieved:** API ergonomics improvements, rendering fixes, code organization

**Phases:**
- [x] Phase 15: API Ergonomics — Reduce Boilerplate (2/2 plans)
- [x] Phase 16: Rendering Improvements (5/5 plans)
- [x] Phase 17: Performance Fixes (5/5 plans)
- [x] Phase 17.1: Antimeridian Splitting (1/1 plans) ⚠️ tech debt
- [x] Phase 18: Code Organization (3/3 plans)
- [x] Phase 19: Documentation Fixes (1/1 plans)
- [x] Phase 20: Layer Features (2/2 plans)

**Archive:** [v1.4.0-ROADMAP.md](milestones/v1.4.0-ROADMAP.md)

</details>

<details>
<summary>✅ v1.3.0 Performance (Phases 11-14) — SHIPPED 2026-03-07</summary>

**Achieved:** 1533x average speedup (target: 10x+)

**Phases:**
- [x] Phase 11: Batch Projection (2/2 plans)
- [x] Phase 12: Viewport Caching (3/3 plans)
- [x] Phase 13: Integration & Validation (2/2 plans)
- [x] Phase 14: Refactoring & Cleanup (6/6 plans)

**Archive:** [v1.3.0-ROADMAP.md](milestones/v1.3.0-ROADMAP.md)

</details>

<details>
<summary>✅ v1.2.0 API & Examples (Phases 07-10) — SHIPPED 2026-02-27</summary>

**Phases:**
- [x] Phase 07: Data Inspection
- [x] Phase 08: Rendering Improvements
- [x] Phase 09: API Design
- [x] Phase 10: Documentation & Examples

**Archive:** [v1.2.0-ROADMAP.md](milestones/v1.2.0-ROADMAP.md)

</details>

<details>
<summary>✅ v1.1.0 API Improvements — SHIPPED 2026-02-27</summary>

**Archive:** [v1.1.0-ROADMAP.md](milestones/v1.1.0-ROADMAP.md)

</details>

<details>
<summary>✅ v1.0.0 Foundation — SHIPPED 2026-02-22</summary>

**Archive:** [v1.0.0-ROADMAP.md](milestones/v1.0.0-ROADMAP.md)

</details>

---

## Dependency Graph

```
v1.3.0 Performance (Complete):
Phase 11: Batch Projection
    ↓
Phase 12: Viewport Caching
    ↓
Phase 13: Integration & Validation
    ↓
Phase 14: Refactoring & Cleanup

v1.4.0 Developer Experience (In Progress):
                    ↓
        ┌───────────────────────────────┐
        │                               │
Phase 15: API Ergonomics      Phase 16: Rendering
        │                               │
Phase 17: Performance Fixes     Phase 20: Layer Features
        │
Phase 18: Code Organization
        │
Phase 19: Documentation
```

---

*Last Updated: 2026-03-07 — v1.3.0 milestone complete, v1.4.0 planning begins*
