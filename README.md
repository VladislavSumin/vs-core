# Available libs

* [conventions](docs/conventions.md)


* [compose](docs/compose.md)
* [coroutines](docs/coroutines.md)
* [decompose](docs/decompose.md)
* [di](docs/di.md)
* [ktor-client](docs/ktor-client.md)
* [ktor-server](docs/ktor-server.md)
* [logging](docs/logging.md)
* [navigation](docs/navigation.md)
* [serialization](docs/serialization.md)
* [uikit](docs/uikit.md)
* [utils](docs/utils.md)

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
