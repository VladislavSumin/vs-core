plugins {
    id("ru.vs.convention.multiplatform.jvm")
    id("ru.vs.convention.multiplatform.android-library")
}

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(project(":core:di"))

                api(coreLibs.ktor.client.core)
                api(coreLibs.ktor.client.serialization)
            }
        }

        named("androidMain") {
            dependencies {
                implementation(coreLibs.ktor.client.android)
            }
        }

        named("jvmMain") {
            dependencies {
                implementation(coreLibs.ktor.client.java)
            }
        }
    }
}
