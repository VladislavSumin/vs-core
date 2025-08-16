package ru.vladislavsumin.utils

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

/**
 * Предоставляет доступ к [BaseExtension] вне зависимости от типа подключенного андроид плагина.
 */
val Project.android: BaseExtension
    get() = extensions.getByType()

/**
 * Предоставляет доступ к [BaseExtension] вне зависимости от типа подключенного андроид плагина.
 */
fun Project.android(block: BaseExtension.() -> Unit) = android.block()
