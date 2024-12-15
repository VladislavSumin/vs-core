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
        setCompileSdkVersion(configuration.core.android.compileSdk)

        defaultConfig {
            minSdk = configuration.core.android.minSdk
            targetSdk = configuration.core.android.targetSdk
        }

        compileOptions {
            val version = JavaVersion.toVersion(configuration.core.jvmVersion)
            sourceCompatibility = version
            targetCompatibility = version
        }
    }
}
