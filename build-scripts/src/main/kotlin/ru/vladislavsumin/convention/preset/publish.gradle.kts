package ru.vladislavsumin.convention.preset

import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension

plugins {
    id("ru.vladislavsumin.convention.publication.sonatype")
    id("ru.vladislavsumin.convention.analyze.binary-validator")
}

// Мы не можем подключить тут плагин kotlin напрямую так как это соглашение может использоваться
// как для multiplatform, так и для jvm проектов.
project.extensions.getByName<KotlinTopLevelExtension>("kotlin").explicitApi()

val publishName = "$group:$name"
mavenPublishing {
    pom {
        name = publishName
        description = "Part of VladislavSumin core libraries"
    }
}
