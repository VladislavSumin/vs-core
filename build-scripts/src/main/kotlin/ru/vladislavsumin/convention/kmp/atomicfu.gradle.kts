package ru.vladislavsumin.convention.kmp

import ru.vladislavsumin.utils.vsCoreLibs

/**
 * Настройки по умолчанию для atomicfu плагина.
 */

plugins {
    id("kotlin-multiplatform")
    id("org.jetbrains.kotlinx.atomicfu")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(vsCoreLibs.kotlin.atomicfu)
        }
    }
}
