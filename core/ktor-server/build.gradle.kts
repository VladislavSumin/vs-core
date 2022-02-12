plugins {
    id("ru.vs.convention.multiplatform.jvm")
}

kotlin {
    sourceSets {
        named("jvmMain") {
            dependencies {
                api(coreLibs.ktor.server.netty)
                api(coreLibs.ktor.server.serialization)
            }
        }
    }
}
