package ru.vladislavsumin.convention.android

/**
 * Расширение базовой android настройки, включает в себя настройку kotlin.
 *
 * В AGP 9+ Kotlin поддержка встроена в Android плагины (com.android.library, com.android.application),
 * поэтому отдельное применение kotlin("android") больше не требуется.
 * JVM target настраивается через compileOptions в convention.android.base.
 */

plugins {
    id("ru.vladislavsumin.convention.android.base")
}
