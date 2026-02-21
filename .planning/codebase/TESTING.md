# Testing Patterns

**Analysis Date:** 2026-02-21

## Test Framework

**Runner:**
- JUnit 4.13.2
- Config: Gradle built-in test task (no custom configuration)

**Assertion Library:**
- JUnit built-in assertions (not configured)
- No additional assertion libraries (e.g., Kotest, AssertJ) configured

**Run Commands:**
```bash
./gradlew test              # Run all tests
./gradlew build             # Build and test (runs test task)
./gradlew testClasses       # Compile test classes only
```

## Test File Organization

**Location:**
- Expected: `src/test/kotlin/`
- **Current state: No test directory exists**
- Tests should be co-located in standard Maven/Gradle structure

**Naming:**
- Standard convention: `*Test.kt` suffix (e.g., `MyClassTest.kt`)
- No existing test files to verify pattern

**Expected Structure:**
```
src/
├── main/
│   └── kotlin/
│       ├── TemplateProgram.kt
│       └── TemplateLiveProgram.kt
└── test/                    # Directory does not exist yet
    └── kotlin/
        └── *Test.kt
```

## Test Structure

**Suite Organization:**
- No existing tests to document
- Expected JUnit 4 pattern:
```kotlin
import org.junit.Test
import org.junit.Assert.*

class ExampleTest {
    @Test
    fun testSomething() {
        assertEquals(expected, actual)
    }
}
```

**Patterns:**
- No setup/teardown patterns established
- No test base classes or utilities defined

## Mocking

**Framework:** None configured

**Available Options:**
- No mocking framework currently included (MockK, Mockito, etc.)
- Would need to add dependency to use mocking

**What to Mock:**
- OPENRNDR components (drawer, window) would require mocking for unit tests
- File I/O operations
- Native dependencies

**What NOT to Mock:**
- Pure Kotlin logic
- Data classes and simple transformations

## Fixtures and Factories

**Test Data:**
- No test fixtures defined
- Could potentially reuse `data/` directory assets for integration tests

**Location:**
- Test resources expected at: `src/test/resources/`
- No test resources directory exists

## Coverage

**Requirements:** None enforced

**View Coverage:**
```bash
./gradlew test               # Basic test run (no coverage report)
```

**Coverage Tools:**
- No coverage plugin configured (JaCoCo, Kotlinx.kover, etc.)
- CI does not report coverage

## Test Types

**Unit Tests:**
- Not currently implemented
- Would test: utility functions, calculations, data transformations

**Integration Tests:**
- Not currently implemented
- Would test: asset loading, configuration parsing
- Challenge: OPENRNDR requires display/GPU context

**E2E Tests:**
- Not used
- Visual output testing would require specialized setup

## CI/CD Testing

**GitHub Actions:**
- `build-on-commit.yaml` runs `./gradlew build` (includes test task)
- Tests would run automatically if present
- No test result reporting configured

**Build Configuration:**
```yaml
# From .github/workflows/build-on-commit.yaml
- name: Build sources
  run: ./gradlew build
```

## Test Dependencies

**Currently Configured:**
```kotlin
// build.gradle.kts
testImplementation(libs.junit)  // JUnit 4.13.2
```

**Not Configured (would need to add):**
- MockK or Mockito for mocking
- Kotest or AssertJ for assertions
- Kotlin test libraries
- JaCoCo/Kover for coverage

## Challenges for Testing OPENRNDR

**Display Context:**
- OPENRNDR programs require OpenGL context
- Headless testing requires special configuration
- `openrndr-gl3` requires GPU/display

**Live Program Testing:**
- `oliveProgram` adds complexity for testing
- Hot-reload behavior may interfere with test isolation

**Recommended Approach:**
1. Extract pure logic into testable functions
2. Use dependency injection for OPENRNDR components
3. Mock drawer and rendering calls for unit tests
4. Integration tests for asset loading (may need test-specific data)

## Adding Tests to This Project

**Step 1: Create test directory structure:**
```bash
mkdir -p src/test/kotlin
mkdir -p src/test/resources
```

**Step 2: Add test dependencies (optional enhancements):**
```kotlin
// build.gradle.kts dependencies block
testImplementation("io.mockk:mockk:1.13.8")
testImplementation("org.assertj:assertj-core:3.24.2")
```

**Step 3: Create first test:**
```kotlin
// src/test/kotlin/SampleTest.kt
import org.junit.Test
import org.junit.Assert.*

class SampleTest {
    @Test
    fun `basic assertion`() {
        assertEquals(4, 2 + 2)
    }
}
```

**Step 4: Run tests:**
```bash
./gradlew test
```

## Test Coverage Gaps

**Untested Areas:**
- All source code in `src/main/kotlin/`
- Build configuration logic
- Asset loading functionality

**Files:**
- `src/main/kotlin/TemplateProgram.kt` - No tests
- `src/main/kotlin/TemplateLiveProgram.kt` - No tests

**Risk:**
- Any refactoring could introduce regressions undetected
- No verification of visual output correctness

**Priority:** Medium
- Template project may not need extensive tests
- Real projects should add tests for business logic

---

*Testing analysis: 2026-02-21*
