plugins {
    id("ru.vs.convention.multiplatform.jvm")
}

kotlin {
    sourceSets {
        named("commonMain") {
            dependencies {
                api(project(":core:logging"))
            }
        }
        named("jvmMain") {
            dependencies {
                implementation(coreLibs.logging.log4j.api)
                implementation(coreLibs.logging.log4j.core)
                implementation(coreLibs.logging.log4j.slf4j)
                implementation(coreLibs.logging.slf4j)
            }
        }
    }
}
