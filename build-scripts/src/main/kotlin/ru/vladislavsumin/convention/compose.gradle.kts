package ru.vladislavsumin.convention

import org.jetbrains.compose.ExperimentalComposeLibrary

/**
 * Настройки по умолчанию для jetbrains compose плагина.
 */

plugins {
    id("ru.vladislavsumin.convention.kmp.common")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Основной compose runtime, не тянет за собой ничего лишнего
            implementation(compose.runtime)

            // Compose UI, не содержит в себе материал компоненты, только базовые элементы
            implementation(compose.ui)

        }
        commonTest.dependencies {
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }
    }
}
