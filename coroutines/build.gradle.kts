plugins {
    id("convention.multiplatform.jvm")
}

group = "ru.vs"

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                api(coreLibs.kotlin.coroutines.core)
            }
        }
    }
}
