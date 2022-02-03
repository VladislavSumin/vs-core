plugins {
    id("convention.multiplatform.jvm")
    id("maven-publish")
}

group = "ru.vs"
version = "0.1.0"


kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                api(coreLibs.kotlin.coroutines.core)
            }
        }
    }
}
