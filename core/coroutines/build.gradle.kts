
plugins {
    id("ru.vs.convention.multiplatform.jvm")
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

publishing {
    this.repositories {
        maven("https://sumin.jfrog.io/artifactory/vs/") {
            credentials {
                username = "deployer"
                password = "usxb5Y50E5U="
            }
        }
    }
}
