# Coding Conventions

**Analysis Date:** 2026-02-21

## Naming Patterns

**Files:**
- PascalCase for source files: `TemplateProgram.kt`, `TemplateLiveProgram.kt`
- kebab-case for Gradle config: `build.gradle.kts`, `settings.gradle.kts`
- kebab-case for version catalog: `libs.versions.toml`

**Functions:**
- camelCase for function names
- Top-level functions allowed: `fun main() = application {}`
- DSL-style lambdas for configuration blocks

**Variables:**
- camelCase for all variables and properties
- Examples from `build.gradle.kts`: `applicationMainClass`, `orxFeatures`, `ormlFeatures`, `openrndrVersion`
- Constants also use camelCase (not SCREAMING_SNAKE_CASE): `applicationLogging`, `orxTensorflowBackend`

**Types:**
- PascalCase for classes and enums: `Openrndr`, `Logging`
- Type aliases used for clarity: `val applicationMainClass = "TemplateProgramKt"`

## Code Style

**Formatting:**
- Kotlin official code style enforced via `kotlin.code.style=official` in `gradle.properties`
- Indentation: 4 spaces (Kotlin standard)
- Maximum line length: 120 characters (Kotlin default)

**Linting:**
- No additional linting tools configured (e.g., detekt, ktlint)
- Relies on Kotlin compiler and IDE enforcement

**Bracing:**
- Opening brace on same line
- Lambda expressions use trailing lambda pattern

## Import Organization

**Order:**
1. Kotlin standard library imports
2. External library imports (OPENRNDR, kotlinx, etc.)
3. No explicit import grouping beyond alphabetical

**Pattern observed in `src/main/kotlin/TemplateProgram.kt`:**
```kotlin
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import org.openrndr.draw.loadImage
import org.openrndr.extra.color.colormatrix.tint
import kotlin.math.cos
import kotlin.math.sin
```

**Path Aliases:**
- No path aliases configured
- Full qualified imports used

**Wildcard Imports:**
- Not used; explicit imports preferred

## Error Handling

**Patterns:**
- Exception throwing for invalid configuration:
```kotlin
// From build.gradle.kts
throw IllegalArgumentException("target platform not supported: $platform")
throw IllegalArgumentException("architecture not supported: $currArch")
throw IllegalArgumentException("os not supported: ${currOs.name}")
```

**Null Safety:**
- Kotlin null safety features used
- `setOfNotNull()` for filtering nulls: `val openrndrFeatures = setOfNotNull(...)`

**Validation:**
- Input validation in configuration classes
- Platform support checked at runtime

## Logging

**Framework:** SLF4J with configurable backends

**Configuration in `build.gradle.kts`:**
```kotlin
enum class Logging { NONE, SIMPLE, FULL }
val applicationLogging = Logging.FULL

when (applicationLogging) {
    Logging.NONE -> runtimeOnly(libs.slf4j.nop)
    Logging.SIMPLE -> runtimeOnly(libs.slf4j.simple)
    Logging.FULL -> {
        runtimeOnly(libs.log4j.slf4j2)
        runtimeOnly(libs.log4j.core)
        runtimeOnly(libs.jackson.databind)
        runtimeOnly(libs.jackson.json)
    }
}
```

**Usage:**
- `kotlin-logging` library available: `implementation(libs.kotlin.logging)`
- Log file location: `application.log` (gitignored)

**When to Log:**
- Application-level logging, not per-frame in render loops

## Comments

**When to Comment:**
- KDoc comments for program documentation
- Inline comments for feature flags and configuration options

**KDoc/TSDoc:**
```kotlin
/**
 *  This is a template for a live program.
 *
 *  It uses oliveProgram {} instead of program {}. All code inside the
 *  oliveProgram {} can be changed while the program is running.
 */
```

**Section Markers:**
- Divider comments used in build files:
```kotlin
// ------------------------------------------------------------------------------------------------------------------ //
```

**Commented Code:**
- Feature flags left as commented options:
```kotlin
val orxFeatures = setOf<String>(
//  "orx-axidraw",
//  "orx-boofcv",
    "orx-camera",
    ...
)
```

## Function Design

**Size:** Functions typically small and focused

**Parameters:**
- Named parameters in DSL blocks
- Lambda receivers for configuration: `configure { width = 768 }`

**Return Values:**
- Unit functions for side effects (rendering)
- Expression body functions for simple returns:
```kotlin
fun orx(module: String) = "org.openrndr.extra:$module:$orxVersion"
```

## Module Design

**Exports:**
- Single public class/object per file pattern
- Top-level functions as entry points

**Barrel Files:** Not used

**Package Structure:**
- Default package used in templates (no explicit `package` declaration)
- For real projects, packages expected: `src/main/kotlin/foo/bar/myProgram.kt` → `package foo.bar`

## Build Configuration Patterns

**Version Catalog:**
- Centralized versions in `gradle/libs.versions.toml`
- Type-safe access: `libs.versions.openrndr.get()`

**Feature Toggles:**
- Set-based feature selection:
```kotlin
val orxFeatures = setOf<String>(
    "orx-camera",
    "orx-color",
    ...
)
for (feature in orxFeatures) {
    implementation(orx(feature))
}
```

**Platform Detection:**
- Runtime platform detection for native dependencies
- Cross-build support via `-PtargetPlatform=<platform>`

## OPENRNDR-Specific Conventions

**Program Structure:**
```kotlin
fun main() = application {
    configure {
        width = 768
        height = 576
    }
    program {
        // Setup code
        extend {
            // Render loop
        }
    }
}
```

**Live Coding Pattern:**
```kotlin
fun main() = application {
    configure { /* window settings */ }
    oliveProgram {
        extend {
            // Hot-reloadable code
        }
    }
}
```

**Asset Loading:**
- Assets stored in `data/` directory
- Loading functions: `loadImage("data/images/...")`, `loadFont("data/fonts/...", size)`

---

*Convention analysis: 2026-02-21*
