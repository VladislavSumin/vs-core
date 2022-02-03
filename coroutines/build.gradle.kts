plugins {
    id("ru.vs.convention.multiplatform.jvm")
    id("maven-publish")
}

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                api(coreLibs.kotlin.coroutines.core)
            }
        }
    }
}
