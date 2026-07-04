package ru.vladislavsumin.convention.analyze

import dev.detekt.gradle.Detekt
import ru.vladislavsumin.utils.protectFromDslAccessors
import ru.vladislavsumin.utils.vsCoreLibs

/**
 * Настройка detekt плагина по умолчанию. Должна подключаться ко всем модулям в которых нужен detekt.
 */

plugins {
    id("dev.detekt")
}

// Почему важно разделять таски созданные плагином и таски созданные вручную?
// По умолчанию мы хотим с помощью детекта анализировать только код внутри модуля, но иногда нам могут потребоваться
// дополнительные кастомные detekt таски, которые будут анализировать код по другим путям.

// Конфигурируем на уровне тасок, а не на уровне плагина, так как таски созданные в ручную не подтягивают дефолтные
// значения из конфигурации плагина, а мы хотим применить дефолтный конфиг ко всем таскам.
val resolveConfigTask = rootProject.tasks.named("resolveDetektBaseConfig")
val writeCustomConfigTask = rootProject.tasks.named("writeDetektCustomConfig")

tasks.withType<Detekt>().configureEach {
    dependsOn(resolveConfigTask)
    dependsOn(writeCustomConfigTask)
    autoCorrect = true
    parallel = true
    buildUponDefaultConfig = true
    config.setFrom(
        rootProject.layout.buildDirectory.file("tmp/detekt-base-config.yml"),
        rootProject.layout.buildDirectory.file("tmp/detekt-custom-config.yml"),
    )
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

protectFromDslAccessors {
    dependencies {
        // Добавляет проверку форматирования кода.
        detektPlugins(vsCoreLibs.detekt.formatting)

        // Кастомные правила detekt. Внутри vs-core используется project-зависимость,
        // в потребляющих проектах — version catalog с авто-подстановкой через includeBuild.
        val customRulesProject = rootProject.findProject(":core:custom-detekt:rules")
        if (customRulesProject != null) {
            detektPlugins(customRulesProject)
        } else {
            detektPlugins(vsCoreLibs.vs.core.customDetekt.rules)
        }
    }
}
