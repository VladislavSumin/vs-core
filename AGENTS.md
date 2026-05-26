# AGENTS.md

## Build system

- **Java 21 required** — enforced at root `build.gradle.kts:11`.
- Gradle config cache, parallel, and caching are on (`gradle.properties`).

### Included build (`build-scripts/`)

Custom convention plugins live as precompiled script plugins in `build-scripts/src/main/kotlin/ru/vladislavsumin/convention/`. They are accessible only because `build-scripts` is an included build (`settings.gradle.kts:4`).

## Convention plugins (presets for modules)

Most KMP modules use two plugins:
- `ru.vladislavsumin.convention.kmp.all` — all KMP targets (Android library + JVM + JS + iOS + macOS + Wasm)
- `ru.vladislavsumin.convention.preset.publish` — Maven Central publication + binary API validation

**KSP code-generator modules** use instead:
- `ru.vladislavsumin.convention.preset.ksp-code-generator` — JVM-only, auto-depends on `:core:ksp:utils`, test dep on `:core:ksp:test`

**Test-only modules** (e.g. `:core:decompose:test`) do NOT use `publish`, they use the `group`+`version` conventions individually.

**Compose-UI modules** add `ru.vladislavsumin.convention.compose` (already includes `kmp.common`).

## Module structure

```
core/<domain>/           — parent directory (NOT a Gradle project, just a grouping folder)
core/<domain>/api/       — public API module
core/<domain>/impl/      — implementation module (auto-depends on sibling `api` via impl-to-api-dependency convention)
core/<domain>/build.gradle.kts  — sometimes a real module under the domain directly (e.g. `core/di`)
```

The `impl-to-api-dependency` convention (`build-scripts/.../impl-to-api-dependency.gradle.kts`) automatically adds an `api(project(":<parent>:api"))` dependency to any module whose name ends with `impl` that has a sibling module named `api`.

## KSP modules

KSP processors that need to generate common metadata sources must use the `ksp-hack` convention. After applying it, add KSP dependencies with `add("kspCommonMainMetadata", ...)` — NOT the standard `ksp(...)` configuration.

Tests for KSP processors use the `:core:ksp:test` module (wraps Kotlin Compile Testing for KSP). See `core/factory-generator/ksp/src/test/` for examples.

## Commands

```bash
# All checks (same order as CI)
./gradlew detekt test allTests apiCheck assemble

# Single task
./gradlew detekt
./gradlew test              # JVM/Android unit tests only
./gradlew allTests          # all platform tests (JS, native, etc.)
./gradlew apiCheck          # binary compatibility check

# Run tests for a specific module
./gradlew :core:navigation:impl:test

# Run a single test class
./gradlew :core:factory-generator:ksp:test --tests "ru.vladislavsumin.core.factoryGenerator.FactoryGeneratorSymbolProcessorTest"

# Assemble all
./gradlew assemble
```

## CI

- **Detekt** first (separate job, `ubuntu-24.04`)
- **Unit tests** (`test allTests`, `ubuntu-24.04`)
- **API check** (`apiCheck`, `ubuntu-24.04`)
- **Assemble** (verify compilation, `ubuntu-24.04`)
- **Publish**: triggered by `v*` tags on `macos-15`, runs `publishToMavenCentral`

## Detekt

- Config at `config/analyze/detekt.yml`
- Applied to ALL projects via root convention (`detekt-all`). Each module scans `src/**/*`, `build.gradle.kts`, `settings.gradle.kts`.
- `autoCorrect = true` — detekt will auto-fix issues.

## API compatibility

The `publish` preset includes `binary-compatibility-validator`. Any change to public API requires running `./gradlew apiDump` to update `.api` snapshot files. CI will fail on `apiCheck` if these are stale.

## Publishing

- Version is set via `-Pru.vs.version=X.Y.Z` (defaults to `0.0.1`)
- Base package: `ru.vladislavsumin` (gradle.properties `ru.vs.basePackage`)
- Group is auto-derived: `ru.vladislavsumin.<parent-module-path.dots>` (e.g. `ru.vladislavsumin.core.collections` for `:core:collections:tree`)

## Version catalog

`libs.versions.toml` is loaded as `vsCoreLibs` in all build scripts via `common-settings.gradle.kts`. All dependencies must go through the catalog.
