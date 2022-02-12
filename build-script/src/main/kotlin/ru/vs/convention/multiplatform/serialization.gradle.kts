package ru.vs.convention.multiplatform

import org.gradle.accessors.dm.LibrariesForCoreLibs
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.the

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

afterEvaluate {
    val coreLibs = the<LibrariesForCoreLibs>()

    kotlin {
        sourceSets {
            named("commonMain") {
                dependencies {
                    implementation(coreLibs.kotlin.serialization.core)
                }
            }
        }
    }
}
