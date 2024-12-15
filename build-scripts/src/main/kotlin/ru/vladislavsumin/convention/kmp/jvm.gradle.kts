package ru.vladislavsumin.convention.kmp

/**
 * Базовая настройка JVM таргета для KMP.
 */

plugins {
    id("ru.vladislavsumin.convention.kmp.common")
}

kotlin {
    jvm()

    sourceSets {
        jvmTest {
            dependencies {
                implementation(kotlin("test-junit5"))
            }
        }
    }
}
