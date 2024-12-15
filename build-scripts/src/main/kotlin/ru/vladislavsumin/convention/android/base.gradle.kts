package ru.vladislavsumin.convention.android

import ru.vladislavsumin.configuration.projectConfiguration
import ru.vladislavsumin.utils.android
import ru.vladislavsumin.utils.protectFormDslAccessors

/**
 * Базовая настройка android плагина без привязки к конкретной имплементации (application/library/итд).
 */

val configuration = project.projectConfiguration

protectFormDslAccessors {
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
