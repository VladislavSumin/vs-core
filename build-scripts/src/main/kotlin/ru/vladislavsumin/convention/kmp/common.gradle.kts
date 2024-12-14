package ru.vladislavsumin.convention.kmp

import ru.vladislavsumin.utils.libs

/**
 * Базовая настройка KMP.
 */

plugins {
    id("kotlin-multiplatform")
}

kotlin {
    // Включаем автоматическую генерацию source set`ов. Подробнее читайте документацию по функции.
    applyDefaultHierarchyTemplate()

    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.kotlin.coroutines.test)
            }
        }
    }
}

// Настраиваем JUnit тесты
tasks.withType<Test> {
    useJUnitPlatform()
}
