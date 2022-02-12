plugins {
    `kotlin-dsl`
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.16.0"
}

val pBuildNumber: String? by project
group = "ru.vs"
version = "0.0.1.${pBuildNumber ?: "1"}"

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

// TODO дублирование кода
val passwordFromEnvironment = System.getenv("VS_MAVEN_PASSWORD") ?: ""

publishing {
    this.repositories {
        maven("https://sumin.jfrog.io/artifactory/vs/") {
            credentials {
                username = "deployer"
                password = passwordFromEnvironment
            }
        }
    }
}
