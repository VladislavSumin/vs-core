package ru.vladislavsumin.convention.android

import ru.vladislavsumin.configuration.projectConfiguration
import ru.vladislavsumin.utils.android
import ru.vladislavsumin.utils.protectFromDslAccessors

/**
 * Базовая настройка android плагина без привязки к конкретной имплементации (application/library/итд).
 */

val configuration = project.projectConfiguration

protectFromDslAccessors {
    android {
        compileSdk = configuration.core.android.compileSdk
        defaultConfig.minSdk = configuration.core.android.minSdk
        compileOptions.sourceCompatibility = JavaVersion.toVersion(configuration.core.jvmVersion)
        compileOptions.targetCompatibility = JavaVersion.toVersion(configuration.core.jvmVersion)
    }
}
