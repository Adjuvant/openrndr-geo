# Codebase Concerns

**Analysis Date:** 2026-02-21

## Tech Debt

**Outdated Testing Framework:**
- Issue: Using JUnit 4.13.2 when JUnit 5 (Jupiter) is the modern standard
- Files: `gradle/libs.versions.toml` (line 10)
- Impact: Missing modern testing features (parameterized tests, better assertions, nested tests)
- Fix approach: Migrate to JUnit 5 with `junit-jupiter` artifacts

**Commented-Out Configuration:**
- Issue: Gradle configuration cache is disabled via comments
- Files: `gradle.properties` (lines 3-5)
- Impact: Slower build times, missing performance optimization
- Fix approach: Investigate why cache is disabled, fix underlying issues, enable it

**No Package Declarations:**
- Issue: Source files lack package declarations, using default package
- Files: `src/main/kotlin/TemplateProgram.kt`, `src/main/kotlin/TemplateLiveProgram.kt`
- Impact: Poor code organization, potential naming conflicts, unprofessional structure
- Fix approach: Add appropriate package declarations (e.g., `package org.openrndr.geo`)

## Known Bugs

**None detected** - This is a template project with minimal code. No runtime bugs present in the placeholder implementation.

## Security Considerations

**Log4j2 Dependency:**
- Risk: Log4j has historically had critical vulnerabilities (Log4Shell CVE-2021-44228)
- Files: `gradle/libs.versions.toml` (log4j version 2.23.1)
- Current mitigation: Version 2.23.1 is patched against known major vulnerabilities
- Recommendations: Monitor for future CVEs, consider updating regularly

**Log File Exposure:**
- Risk: `application.log` may capture sensitive information during development
- Files: `src/main/resources/log4j2.yaml` (line 13)
- Current mitigation: File is gitignored
- Recommendations: Ensure production deployments properly secure log files

**No Input Validation:**
- Risk: File paths are hardcoded without validation
- Files: `src/main/kotlin/TemplateProgram.kt` (lines 16-17)
- Current mitigation: None - uses hardcoded demo paths
- Recommendations: Add file existence checks and error handling for production code

## Performance Bottlenecks

**No significant bottlenecks detected** - The template code is minimal with simple rendering operations.

**Potential Future Concern:**
- Problem: Video feature disabled on ARM architecture
- Files: `build.gradle.kts` (lines 93-95)
- Cause: Conditional exclusion: `if (DefaultNativePlatform("current").architecture.name != "arm-v8") "video" else null`
- Impact: No video playback support on Apple Silicon or ARM Linux
- Improvement path: Investigate FFmpeg natives availability for ARM platforms

## Fragile Areas

**Hardcoded Resource Paths:**
- Files: `src/main/kotlin/TemplateProgram.kt` (lines 16-17)
- Why fragile: Direct string paths without existence checks, will crash if files missing
- Safe modification: Wrap file loading in try-catch or use resource existence validation
- Test coverage: None - no tests for file loading

**Live Program Reloading:**
- Files: `src/main/kotlin/TemplateLiveProgram.kt`
- Why fragile: `oliveProgram` enables hot code reloading which can lead to inconsistent state if not carefully managed
- Safe modification: Test thoroughly after live edits, restart program for major changes
- Test coverage: None

**Demo Asset Dependencies:**
- Files: `data/images/pm5544.png`, `data/images/cheeta.jpg`, `data/fonts/default.otf`
- Why fragile: Template code depends on these specific files existing
- Safe modification: Ensure any new projects either keep these files or update source code
- Test coverage: None

## Scaling Limits

**Single-Program Architecture:**
- Current capacity: Single main class per run
- Limit: Cannot easily run multiple OPENRNDR programs simultaneously
- Scaling path: Create separate modules or use programmatic configuration switching

**No Modularization:**
- Current capacity: All code in root of `src/main/kotlin/`
- Limit: Will become unwieldy as project grows
- Scaling path: Introduce package structure with domain-driven organization

## Dependencies at Risk

**JUnit 4:**
- Risk: End-of-life framework, no new features
- Impact: Missing modern testing capabilities, eventual deprecation
- Migration plan: Update to JUnit 5 (`org.junit.jupiter:junit-jupiter:5.x.x`)

**Kotlin 2.2.10:**
- Risk: Kotlin updates frequently, may need updates for compatibility
- Impact: May miss language improvements and bug fixes
- Migration plan: Regular version updates as part of maintenance

## Missing Critical Features

**No Test Suite:**
- Problem: Zero test files despite JUnit dependency
- Files: Entire `src/test/` directory is absent
- Blocks: CI/CD confidence, refactoring safety, regression prevention

**No Documentation:**
- Problem: Only template README exists, no code documentation
- Files: All source files
- Blocks: Knowledge transfer, maintenance by other developers

**No Error Handling:**
- Problem: No try-catch blocks, no null safety patterns, no error logging
- Files: `src/main/kotlin/TemplateProgram.kt`, `src/main/kotlin/TemplateLiveProgram.kt`
- Blocks: Graceful degradation, debugging production issues

## Test Coverage Gaps

**Complete Absence of Tests:**
- What's not tested: All functionality (image loading, font loading, rendering, live reloading)
- Files: `src/main/kotlin/*.kt`
- Risk: Any refactoring could silently break functionality
- Priority: High

**No Test Directory Structure:**
- What's not tested: N/A - no test infrastructure exists
- Files: `src/test/kotlin/` does not exist
- Risk: Cannot add tests without creating structure
- Priority: High

---

*Concerns audit: 2026-02-21*
