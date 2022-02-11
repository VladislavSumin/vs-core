plugins {
    id("ru.vs.convention.multiplatform.android-library")
    id("ru.vs.convention.multiplatform.jvm")
    id("org.jetbrains.compose")
}

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                api(coreLibs.decompose.core)
                api(coreLibs.decompose.jetbrains)

                api(coreLibs.kodein.compose)

                implementation(project(":core:compose"))
                implementation(project(":core:logging"))
            }
        }
        named("androidMain") {
            dependencies {
                api(coreLibs.decompose.android)
            }
        }
    }
}
