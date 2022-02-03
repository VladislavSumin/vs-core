plugins {
    `kotlin-dsl`
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.16.0"
}

group = "ru.vs"
version = "0.1.0"

dependencies {
    implementation(coreLibs.gradlePlugins.kotlin.core)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

gradlePlugin {
    plugins {
        create("EmptyPlugin") {
            id = "ru.vs.empty_plugin"
            implementationClass = "ru.vs.build_script.EmptyPlugin"
        }
    }
}
