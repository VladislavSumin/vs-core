package ru.vladislavsumin.convention.kmp

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import ru.vladislavsumin.configuration.projectConfiguration
import ru.vladislavsumin.utils.fullNameAsNamespace
import ru.vladislavsumin.utils.protectFromDslAccessors

/**
 * Базовая настройка android таргета для KMP (без привязки к library/application).
 */

plugins {
    id("ru.vladislavsumin.convention.kmp.common")
    id("ru.vladislavsumin.convention.kmp.android-tests")
    id("com.android.kotlin.multiplatform.library")
}

val configuration = project.projectConfiguration

protectFromDslAccessors {
    kotlin {
        android {
            namespace = "${configuration.basePackage}.${project.fullNameAsNamespace()}"
            compileSdk = configuration.core.android.compileSdk
            minSdk = configuration.core.android.minSdk

            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(configuration.core.jvmVersion))
            }

            androidResources { enable = true }
        }
    }
}
