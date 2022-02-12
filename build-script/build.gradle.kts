plugins {
    `kotlin-dsl`
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.16.0"
}

group = "ru.vs"
version = "0.1.0"

dependencies {
    // TODO подождать пока эта фича появится в гредле
    // а пока костыль вот отсюда https://github.com/gradle/gradle/issues/15383
    implementation(files(coreLibs.javaClass.superclass.protectionDomain.codeSource.location))

    api(coreLibs.gradlePlugins.kotlin.core)
    api(coreLibs.gradlePlugins.kotlin.serialization)
    api(coreLibs.gradlePlugins.android)
    api(coreLibs.gradlePlugins.jb.compose)
    api(coreLibs.gradlePlugins.checkUpdates)
    api(coreLibs.gradlePlugins.detekt)
    api(coreLibs.gradlePlugins.moko.resources)
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
