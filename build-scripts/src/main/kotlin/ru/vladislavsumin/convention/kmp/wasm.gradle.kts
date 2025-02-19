package ru.vladislavsumin.convention.kmp

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

/**
 * Базовая настройка wasm таргета для KMP.
 */

plugins {
    id("ru.vladislavsumin.convention.kmp.common")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        named("wasmJsTest").dependencies {
            implementation(kotlin("test-wasm-js"))
        }
    }
}
