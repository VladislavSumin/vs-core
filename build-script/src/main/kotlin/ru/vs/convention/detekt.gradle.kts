package ru.vs.convention

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import java.util.Properties

plugins {
    id("io.gitlab.arturbosch.detekt")
}

// Конфигурирем на уровне тасок, а не на уровне плагина, так как таски созданные в ручную
// не подтягивают дефолтные значения из конфигурации плагина
tasks.withType<Detekt>().configureEach {
    autoCorrect = true
    parallel = true
    buildUponDefaultConfig = true
    config.setFrom(files("${project.rootProject.projectDir}/config/detekt/detekt.yml"))
}

// Исправляем путь к файлам только для дефолтных detekt тасок
tasks.named<Detekt>("detekt").configure {
    source = fileTree(project.projectDir.resolve("src"))
}

// TODO поправить костыль
// Из конвеншенов нельзя обратиться к каталогу версий гредла, пришлось добавить вот такой костыль
fun loadDetektVersion(): String = Properties().run {
    load(DetektPlugin::class.java.classLoader.getResourceAsStream("versions.properties"))
    getProperty("detektVersion")
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${loadDetektVersion()}")
}
