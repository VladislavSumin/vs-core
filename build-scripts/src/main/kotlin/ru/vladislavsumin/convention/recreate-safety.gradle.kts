package ru.vladislavsumin.convention

import ru.vladislavsumin.utils.protectFromDslAccessors

/**
 * Подключает IR-плагин компилятора для проверки recreate-safety всех вызовов `viewModel {}`.
 *
 * ## Что делает этот конвеншн
 *
 * 1. Добавляет аннотацию `@RecreateSafe` в compileOnly зависимость модуля.
 *    Это нужно чтобы разработчик мог ставить `@RecreateSafe` на свои типы.
 *
 * 2. Добавляет JAR с IR-плагином в `kotlinCompilerPluginClasspath`.
 *    Это заставляет компилятор Kotlin загрузить плагин и выполнять проверки при каждой компиляции.
 *
 * ## Как использовать
 *
 * В build.gradle.kts модуля добавить плагин:
 * ```kotlin
 * plugins {
 *     id("ru.vladislavsumin.convention.kmp.all")
 *     id("ru.vladislavsumin.convention.recreate-safety")  // ← добавить эту строку
 * }
 * ```
 *
 * После этого при компиляции плагин проверит ВСЕ вызовы `viewModel {}` в модуле.
 *
 * ## Где применять
 *
 * Этот конвеншн нужно применять к модулям, в которых есть Screen/Component с вызовами `viewModel {}`.
 * Обычно это:
 * - :feature:*:impl модули
 * - :core:* модули с UI-компонентами
 *
 * Он НЕ нужен для:
 * - :api модулей (там нет `viewModel {}`)
 * - :core:* модулей без UI (чистая логика без компонентов)
 * - Модулей, где нет Screen/Component
 */

plugins {
    // Нам нужен Kotlin Gradle плагин чтобы kotlinCompilerPluginClasspath был доступен.
    // kotlin-multiplatform подходит и для KMP, и для JVM-only модулей.
    id("kotlin-multiplatform")
}

// Шаг 1: делаем аннотацию @RecreateSafe доступной в исходном коде модуля.
// compileOnly — потому что аннотация нужна только на этапе компиляции,
// в рантайме она не используется.
protectFromDslAccessors {
    kotlin {
        sourceSets {
            commonMain {
                dependencies {
                    compileOnly(project(":core:recreate-safety:api"))
                }
            }
            jvmMain {
                dependencies {
                    compileOnly(project(":core:recreate-safety:api"))
                }
            }
        }
    }
}

// Шаг 2: добавляем JAR с IR-плагином в classpath компилятора Kotlin.
//
// kotlinCompilerPluginClasspath — это специальная конфигурация зависимостей в Kotlin Gradle Plugin,
// которая добавляет JAR'ы в classpath компилятора. Компилятор через ServiceLoader находит
// класс CompilerPluginRegistrar из META-INF/services и загружает плагин.
//
// ВАЖНО: эта зависимость НЕ попадает в рантайм classpath приложения.
// Она используется только на этапе компиляции.
project.dependencies {
    add("kotlinCompilerPluginClasspath", project(":core:recreate-safety:plugin"))
}
