plugins {
    id("ru.vs.convention.multiplatform.android-library")
    id("ru.vs.convention.multiplatform.jvm")
    id("org.jetbrains.compose")
}

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)

                // api(compose.uiTooling)
                // api(compose.preview)
            }
        }
        named("jvmMain") {
            dependencies {
                api(compose.desktop.currentOs)
            }
        }
        named("androidMain") {
            dependencies {
                api(coreLibs.android.activity.compose)
            }
        }
    }
}
