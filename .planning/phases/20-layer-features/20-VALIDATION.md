---
phase: 20
slug: layer-features
status: validated
nyquist_compliant: true
wave_0_complete: true
created: 2026-03-25
audited: 2026-03-25
---

# Phase 20 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 |
| **Config file** | Standard Gradle test configuration (build.gradle.kts) |
| **Quick run command** | `./gradlew test --tests "geo.layer.Graticule*" -q` |
| **Full suite command** | `./gradlew test -q` |
| **Estimated runtime** | ~30 seconds for layer tests, ~3 minutes full suite |

---

## Sampling Rate

- **After every task commit:** Run `geo.layer.GraticuleTest` (subset)
- **After every plan wave:** Run full `geo.layer.*` tests
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 30 seconds for layer tests

---

## Per-task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File | Status |
|---------|------|------|-------------|-----------|-------------------|------|--------|
| 20-01-01 | 01 | 1 | LAYER-01 | unit | `./gradlew test --tests "geo.layer.GraticuleSpacingTest"` | ✅ | ✅ green |
| 20-01-02 | 01 | 1 | LAYER-01 | unit | `./gradlew test --tests "geo.layer.GraticuleLineGenerationTest"` | ✅ | ✅ green |
| 20-01-03 | 01 | 1 | LAYER-01 | unit | `./gradlew test --tests "geo.layer.GraticuleAntimeridianTest"` | ✅ | ✅ green |
| 20-02-01 | 02 | 2 | LAYER-01 | unit | `./gradlew test --tests "geo.layer.GraticuleLabelPositionTest"` | ✅ | ✅ green |
| 20-02-02 | 02 | 2 | LAYER-01 | unit | `./gradlew test --tests "geo.layer.GraticuleLabelFormatTest"` | ✅ | ✅ green |
| 20-02-03 | 02 | 2 | LAYER-01 | unit | `./gradlew test --tests "geo.layer.GraticuleDensityTest"` | ✅ | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [x] `src/test/kotlin/geo/layer/GraticuleSpacingTest.kt` — test power-of-10 spacing logic
- [x] `src/test/kotlin/geo/layer/GraticuleLineGenerationTest.kt` — test line coordinate generation
- [x] `src/test/kotlin/geo/layer/GraticuleAntimeridianTest.kt` — test antimeridian splitting
- [x] `src/test/kotlin/geo/layer/GraticuleLabelPositionTest.kt` — test label placement
- [x] `src/test/kotlin/geo/layer/GraticuleLabelFormatTest.kt` — test degree notation formatting
- [x] `src/test/kotlin/geo/layer/GraticuleDensityTest.kt` — test auto-thinning behavior

*All Wave 0 tests created and passing as of 2026-03-25.*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Visual rendering quality | LAYER-01 | Visual verification of line density at various zoom levels | Run UAT program, verify lines render at 1°, 10°, 30°, 90° spacing |

*If none: "All phase behaviors have automated verification."*

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 30s for layer tests
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** 2026-03-25 — all 6 tests verified green

---

## Validation Audit 2026-03-25

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |
| Tests verified | 6/6 green |
