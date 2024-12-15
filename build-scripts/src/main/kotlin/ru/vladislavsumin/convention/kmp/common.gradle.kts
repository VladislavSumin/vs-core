package ru.vladislavsumin.convention.kmp

import ru.vladislavsumin.utils.protectFromDslAccessors
import ru.vladislavsumin.utils.vsCoreLibs

/**
 * Базовая настройка KMP.
 */

plugins {
    id("kotlin-multiplatform")
}

protectFromDslAccessors {
    kotlin {
        // Включаем автоматическую генерацию source set`ов. Подробнее читайте документацию по функции.
        applyDefaultHierarchyTemplate()

        sourceSets {
            commonTest {
                dependencies {
                    implementation(kotlin("test-common"))
                    implementation(kotlin("test-annotations-common"))
                    implementation(vsCoreLibs.kotlin.coroutines.test)
                }
            }
        }
    }

    // Настраиваем JUnit тесты
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
