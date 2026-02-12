package ru.vladislavsumin.convention.analyze

/**
 *  Стандартные настройки проверки бинарной совместимости публичного апи.
 */

plugins {
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

apiValidation {
    // Не учитываем @PublishedApi как публичное api.
    // Вообще с этим могут быть нюансы бинарной совместимости, но сейчас я рассчитываю, что все модули собираются
    // с одинаковой версией библиотеки.
    nonPublicMarkers.add("kotlin.PublishedApi")
}
