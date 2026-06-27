package ru.vladislavsumin.convention.kmp

/**
 * Базовая настройка iOS таргета для KMP.
 */

plugins {
    id("ru.vladislavsumin.convention.kmp.common")
}

kotlin {
    iosArm64()
    iosSimulatorArm64()
}
