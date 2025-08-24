package ru.vladislavsumin.convention.preset

/**
 * Параметры по умолчанию для модулей генерации кода на основе KSP
 */

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core:ksp:utils"))

    testImplementation(kotlin("test"))
    testImplementation(project(":core:ksp:test"))
}

tasks.test {
    useJUnitPlatform()
}
