package ru.vladislavsumin.convention.analyze

import io.gitlab.arturbosch.detekt.Detekt
import ru.vladislavsumin.utils.vsCoreLibs

/**
 * Настройка detekt плагина по умолчанию. Должна подключаться ко всем модулям в которых нужен detekt.
 */

plugins {
    id("io.gitlab.arturbosch.detekt")
}

// Почему важно разделять таски созданные плагином и таски созданные вручную?
// По умолчанию мы хотим с помощью детекта анализировать только код внутри модуля, но иногда нам могут потребоваться
// дополнительные кастомные detekt таски, которые будут анализировать код по другим путям.

// Конфигурируем на уровне тасок, а не на уровне плагина, так как таски созданные в ручную не подтягивают дефолтные
// значения из конфигурации плагина, а мы хотим применить дефолтный конфиг ко всем таскам.
tasks.withType<Detekt>().configureEach {
    autoCorrect = true
    parallel = true
    buildUponDefaultConfig = true
    config.setFrom(rootProject.layout.projectDirectory.file("config/analyze/detekt.yml"))
}

// Дефолтные пути по которым detekt ищет файлы нас не устраивают, поэтому вручную проставляем пути для тасок с
// именем "detekt" - так мы отделяем таски созданные вручную от тех что плагин создает автоматически.
tasks.named<Detekt>("detekt").configure {
    source = fileTree(project.projectDir) {
        include("src/**/*")
        include("build.gradle.kts")
        include("settings.gradle.kts")
    }
}

dependencies {
    // Добавляет проверку форматирования кода.
    detektPlugins(vsCoreLibs.detekt.formatting)
}
