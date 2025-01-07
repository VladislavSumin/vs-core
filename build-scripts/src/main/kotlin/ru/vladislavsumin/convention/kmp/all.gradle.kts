package ru.vladislavsumin.convention.kmp

/**
 * Подключает все поддерживаемые таргеты kotlin kmp разом.
 * Android таргет в данном случае подключается как android библиотека.
 */

plugins {
    id("ru.vladislavsumin.convention.kmp.android-library")
    id("ru.vladislavsumin.convention.kmp.all-non-android")
}
