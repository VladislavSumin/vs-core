package ru.vladislavsumin.convention.kmp

/**
 * Подключает все поддерживаемые таргеты kotlin kmp кроме android, так как android таргет требует явное указание
 * является ли модуль библиотекой или приложением.
 */

plugins {
    id("ru.vladislavsumin.convention.kmp.js")
    id("ru.vladislavsumin.convention.kmp.jvm")
    id("ru.vladislavsumin.convention.kmp.ios")
    id("ru.vladislavsumin.convention.kmp.macos")
    id("ru.vladislavsumin.convention.kmp.wasm")
}
