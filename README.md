# Available libs
* [compose](docs/compose.md)
* [conventions](docs/conventions.md)
* [coroutines](docs/coroutines.md)
* [di](docs/di.md)
* [logging](docs/logging.md)

# Installation

## Plugin installation

### On project level
For work as project:
```kotlin
// settings.gradle.kts
pluginManagement {
    includeBuild("../vs-core/build-script")
    plugins {
        id("ru.vs.empty_plugin") version "<any version>"
    }
}
```
For work as library:
```kotlin
// settings.gradle.kts
pluginManagement {
    plugins {
        id("ru.vs.empty_plugin") version "<version>"
    }
    repositories {
        mavenLocal()
    }
}
```

### Or on build script level

For work as project:
```kotlin
// settings.gradle.kts
includeBuild("../../vs-core/build-script")
```
```kotlin
// build.gradle.kts
dependencies {
    implementation("ru.vs:build-script:<any version>")
}
```
For work as library:
```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenLocal()
    }
}
```
```kotlin
// build.gradle.kts
dependencies {
    implementation("ru.vs:build-script:<version>")
}
```

## Library installation

```kotlin
// settings.gradle.kts

// For work as project
includeBuild("../vs-core")

// OR
// For work as library
repositories {
    mavenLocal()
}
```
