package ru.vladislavsumin.convention.android

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import ru.vladislavsumin.configuration.projectConfiguration

/**
 * Расширение базовой android настройки, включает в себя настройку kotlin.
 */

plugins {
    id("ru.vladislavsumin.convention.android.base")
    kotlin("android")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(project.projectConfiguration.core.jvmVersion))
    }
}
