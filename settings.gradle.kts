apply { from("build-scripts/common-settings.gradle.kts") }

pluginManagement {
    includeBuild("build-scripts")
}

rootProject.name = "vs-core"

include(":core:decompose:components")
include(":core:decompose:test")
