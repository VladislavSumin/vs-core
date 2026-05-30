package ru.vladislavsumin.utils

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

/**
 * Предоставляет доступ к [CommonExtension] вне зависимости от типа подключенного андроид плагина.
 */
val Project.android: CommonExtension
    get() = extensions.getByType()

/**
 * Предоставляет доступ к [CommonExtension] вне зависимости от типа подключенного андроид плагина.
 */
fun Project.android(block: CommonExtension.() -> Unit) = android.block()
