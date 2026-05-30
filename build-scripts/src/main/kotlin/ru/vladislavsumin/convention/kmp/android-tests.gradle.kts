package ru.vladislavsumin.convention.kmp

/**
 * Дополнительная настройка unit тестов для android таргета.
 */

plugins {
    id("kotlin-multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        withHostTest {}
    }
    sourceSets {
        named("androidHostTest") {
            dependencies {
                implementation(kotlin("test-junit5"))
            }
        }
    }
}
