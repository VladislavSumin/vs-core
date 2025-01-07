package ru.vladislavsumin.convention.kmp

/**
 * Базовая настройка MacOS таргета для KMP.
 */

plugins {
    id("ru.vladislavsumin.convention.kmp.common")
}

kotlin {
    macosX64()
    macosArm64()
}
