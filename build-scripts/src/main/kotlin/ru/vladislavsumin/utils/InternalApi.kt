package ru.vladislavsumin.utils

import kotlinx.validation.ApiValidationExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Настраивает доступ без @OptIn к [annotationName], а так же исключает классы помеченные этой
 * аннотацией из публичного апи.
 */
fun Project.internalApi(annotationName: String) {
    extensions.configure<KotlinMultiplatformExtension>("kotlin") {
        sourceSets.all {
            languageSettings.optIn(annotationName)
        }
    }
    extensions.configure<ApiValidationExtension>("apiValidation") {
        nonPublicMarkers.add(annotationName)
    }
}
