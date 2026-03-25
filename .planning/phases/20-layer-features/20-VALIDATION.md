---
phase: 20
slug: layer-features
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-03-25
---

# Phase 20 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 |
| **Config file** | Standard Gradle test configuration (build.gradle.kts) |
| **Quick run command** | `./gradlew test --tests "geo.layer.GraticuleTest" -q` |
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

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 20-01-01 | 01 | 1 | LAYER-01 | unit | `./gradlew test --tests "geo.layer.GraticuleSpacingTest"` | ❌ W0 | ⬜ pending |
| 20-01-02 | 01 | 1 | LAYER-01 | unit | `./gradlew test --tests "geo.layer.GraticuleLineGenerationTest"` | ❌ W0 | ⬜ pending |
| 20-01-03 | 01 | 1 | LAYER-01 | unit | `./gradlew test --tests "geo.layer.GraticuleAntimeridianTest"` | ❌ W0 | ⬜ pending |
| 20-02-01 | 02 | 2 | LAYER-01 | unit | `./gradlew test --tests "geo.layer.GraticuleLabelPositionTest"` | ❌ W0 | ⬜ pending |
| 20-02-02 | 02 | 2 | LAYER-01 | unit | `./gradlew test --tests "geo.layer.GraticuleLabelFormatTest"` | ❌ W0 | ⬜ pending |
| 20-02-03 | 02 | 2 | LAYER-01 | unit | `./gradlew test --tests "geo.layer.GraticuleDensityTest"` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `src/test/kotlin/geo/layer/GraticuleSpacingTest.kt` — test power-of-10 spacing logic
- [ ] `src/test/kotlin/geo/layer/GraticuleLineGenerationTest.kt` — test line coordinate generation
- [ ] `src/test/kotlin/geo/layer/GraticuleAntimeridianTest.kt` — test antimeridian splitting
- [ ] `src/test/kotlin/geo/layer/GraticuleLabelPositionTest.kt` — test label placement
- [ ] `src/test/kotlin/geo/layer/GraticuleLabelFormatTest.kt` — test degree notation formatting
- [ ] `src/test/kotlin/geo/layer/GraticuleDensityTest.kt` — test auto-thinning behavior

*Wave 0 tests must be created before implementing the graticule features.*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Visual rendering quality | LAYER-01 | Visual verification of line density at various zoom levels | Run UAT program, verify lines render at 1°, 10°, 30°, 90° spacing |

*If none: "All phase behaviors have automated verification."*

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 30s for layer tests
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
