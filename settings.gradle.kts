apply { from("build-scripts/common-settings.gradle.kts") }

pluginManagement {
    includeBuild("build-scripts")
}

rootProject.name = "vs-core"

include(":core:collections:tree")

include(":core:decompose:components")
include(":core:decompose:compose")
include(":core:decompose:test")

include(":core:di")

include(":core:factory-generator:api")
include(":core:factory-generator:ksp")

include(":core:ksp:utils")

include(":core:logger:api")
include(":core:logger:common")
include(":core:logger:internal")
include(":core:logger:manager")
include(":core:logger:platform")

include(":core:navigation:api")
include(":core:navigation:debug")
include(":core:navigation:impl")
include(":core:navigation:factory-generator:api")
include(":core:navigation:factory-generator:ksp")

include(":core:uikit:graph")
