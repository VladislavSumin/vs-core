# Installation

## Plugin installation

```kotlin
// settings.gradle.kts
pluginManagement {
    // For work as project
    includeBuild("../vs-core/build-script")
    plugins {
        id("ru.vs.empty_plugin") version "build-script version"
    }

    // OR
    // For work as library
    plugins {
        id("ru.vs.empty_plugin") version "build-script version"
    }
    repositories {
        mavenLocal()
    }
}
```

## Library installation

TODO