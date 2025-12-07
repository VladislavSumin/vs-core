package ru.vladislavsumin.convention.preset

import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension

plugins {
    id("ru.vladislavsumin.convention.publication.group")
    id("ru.vladislavsumin.convention.publication.version")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
}

// Мы не можем подключить тут плагин kotlin напрямую так как это соглашение может использоваться
// как для multiplatform, так и для jvm проектов.
project.extensions.getByName<KotlinTopLevelExtension>("kotlin").explicitApi()
