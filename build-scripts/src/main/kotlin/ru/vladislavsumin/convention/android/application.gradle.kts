package ru.vladislavsumin.convention.android

import com.android.build.api.dsl.ApplicationExtension
import ru.vladislavsumin.configuration.projectConfiguration
import ru.vladislavsumin.utils.protectFromDslAccessors

/**
 * Базовая настройка android application.
 */

plugins {
    id("com.android.application")
    id("ru.vladislavsumin.convention.android.base")
}

val configuration = project.projectConfiguration

protectFromDslAccessors {
    extensions.configure<ApplicationExtension> {
        defaultConfig.targetSdk = configuration.core.android.targetSdk
    }
}
