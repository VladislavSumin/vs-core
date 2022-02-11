package ru.vs.convention

import io.gitlab.arturbosch.detekt.Detekt

plugins {
    id("ru.vs.convention.detekt")
}

val detektBuildScript = tasks.register<Detekt>("detektBuildScript") {
    source = fileTree(project.rootDir).matching {
        include("build-script/src/**/*.kt", "build-script/**/*.gradle.kts")
        exclude("**/build/**")
    }
}

tasks.named("detekt").configure { dependsOn(detektBuildScript) }
