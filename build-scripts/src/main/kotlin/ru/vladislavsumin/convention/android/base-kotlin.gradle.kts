package ru.vladislavsumin.convention.android

import ru.vladislavsumin.configuration.projectConfiguration
import ru.vladislavsumin.utils.android
import ru.vladislavsumin.utils.kotlinOptions

/**
 * Расширение базовой android настройки, включает в себя настройку kotlin.
 */

plugins {
    id("ru.vladislavsumin.convention.android.base")
    kotlin("android")
}

android {
    kotlinOptions {
        jvmTarget = project.projectConfiguration.core.jvmVersion
    }
}
