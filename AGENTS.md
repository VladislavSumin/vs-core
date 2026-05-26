# AGENTS.md

## Project overview

Kotlin Multiplatform library monorepo — a collection of small KMP libraries published to Maven Central. All modules live under `core/` and are wired via `settings.gradle.kts`.

## Requirements

- **Java 21** (Zulu). The build fails at configuration time if any other JDK is active (`root build.gradle.kts` has a `require` check).

## Commands

```sh
./gradlew detekt              # Lint (autoCorrect ON — edits files in place)
./gradlew test allTests       # Run all unit tests across all KMP targets
./gradlew apiCheck            # Verify binary API compatibility (fails if public API changed without updating .api snapshots)
./gradlew assemble            # Build all artifacts
```

Run a single module's tests:
```sh
./gradlew :core:di:allTests
./gradlew :core:navigation:impl:testDebugUnitTest   # Android-specific
```

CI runs all four checks above as separate jobs. Before pushing, at minimum run `detekt` + `test allTests` + `apiCheck`.

After changing public API intentionally, update snapshots with `./gradlew apiDump` and commit the changed `.api` files.

### Test coverage (Kover)

Kover is applied globally via `convention.analyze.kover-all` on the root project, which auto-applies `convention.analyze.kover` to every subproject. No per-module setup needed.

```sh
./gradlew koverHtmlReport     # Combined HTML report for all modules → build/reports/kover/html/index.html
./gradlew koverXmlReport      # Combined XML report (CI / tooling)  → build/reports/kover/report.xml
./gradlew koverLog            # Print coverage summary to stdout
./gradlew koverVerify         # Verify coverage thresholds (fails build if below)
```

Per-module / per-target reports are also available:
```sh
./gradlew :core:di:koverHtmlReportJvm          # JVM-only HTML report for a specific module
./gradlew :core:navigation:impl:koverHtmlReport  # All targets for a module
```

HTML output path per module: `<module>/build/reports/kover/html/index.html`.

## Architecture

### Convention plugins (`build-scripts/`)

Convention plugins live in the `build-scripts/` **composite build**, not `buildSrc`. Key presets:

| Plugin | When to use |
|---|---|
| `convention.kmp.all` | Full KMP (Android + JVM + JS + iOS + macOS + WASM) |
| `convention.kmp.jvm` | JVM-only module |
| `convention.preset.publish` | Publishable library (sonatype + binary-validator + explicitApi) |
| `convention.preset.publish-off` | Same but without maven publish (group + version + binary-validator + explicitApi) |
| `convention.compose` | JetBrains Compose Multiplatform |
| `convention.kmp.ksp-hack` | Required for KSP codegen in KMP common source sets |

### Version catalog

The version catalog accessor is **`vsCoreLibs`**, not the default `libs`. Defined in `libs.versions.toml`, wired via `common-settings.gradle.kts`.

### `protectFromDslAccessors`

All convention plugin code that accesses project extensions must be wrapped in `protectFromDslAccessors { ... }`. This prevents execution during Gradle's DSL accessor generation phase for composite builds.

### api/impl module split

Modules like `navigation` and `fs` split into `api` and `impl` submodules. The `convention.impl-to-api-dependency` plugin auto-adds `api(project(":...:api"))` to impl modules. Convention: impl modules omit "impl" from their published name.

### `internalApi()` utility

Used in impl module `build.gradle.kts` files to:
1. Opt-in the entire module to an internal annotation (e.g., `@InternalNavigationApi`)
2. Exclude annotated classes from binary API validation

Usage: `internalApi("ru.vladislavsumin.core.navigation.InternalNavigationApi")`

### KSP in KMP

For KSP processors that generate common code, apply `convention.kmp.ksp-hack` and add dependencies as:
```kotlin
dependencies {
    add("kspCommonMainMetadata", <ksp-processor>)
}
```

## Conventions

- **`explicitApi()`** is enforced on all publishable modules — every public declaration must have an explicit `public` modifier.
- **Trailing commas** are required (enforced by detekt formatting rules).
- **Compose function naming**: detekt allows uppercase-first function names for Compose.
- Typesafe project accessors are enabled (`projects.core.di` not `project(":core:di")`).
- Detekt also scans `build.gradle.kts` and `settings.gradle.kts` files, not just `src/`.
