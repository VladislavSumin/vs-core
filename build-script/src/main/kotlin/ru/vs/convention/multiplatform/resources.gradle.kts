package ru.vs.convention.multiplatform

import dev.icerock.gradle.MRVisibility
import org.gradle.accessors.dm.LibrariesForCoreLibs

plugins {
    id("ru.vs.convention.multiplatform.jvm")
    id("dev.icerock.mobile.multiplatform-resources")
}

afterEvaluate {
    val coreLibs = the<LibrariesForCoreLibs>()

    kotlin {
        sourceSets {
            named("commonMain") {
                dependencies {
                    implementation(coreLibs.moko.resources.core)
                    implementation(coreLibs.moko.resources.compose)
                }
            }
        }
    }
}

multiplatformResources {
    multiplatformResourcesVisibility = MRVisibility.Internal
}
