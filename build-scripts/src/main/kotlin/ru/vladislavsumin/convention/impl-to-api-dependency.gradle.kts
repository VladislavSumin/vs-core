package ru.vladislavsumin.convention

import ru.vladislavsumin.utils.protectFromDslAccessors

/**
 * Автоматически прописывает *impl модулю api зависимость на *api модуль.
 */

plugins {
    id("kotlin-multiplatform")
}

protectFromDslAccessors {
    kotlin {
        sourceSets {
            commonMain.dependencies {
                val suffix = "impl"
                if (project.name.endsWith(suffix)) {
                    val apiProjectName = project.name.dropLast(suffix.length) + "api"
                    val apiProject = project.parent!!.findProject(apiProjectName)
                    apiProject?.let { api(it) }
                } else {
                    // Не подключаем ничего если не нашли подходящий проект, но и не падаем, так как это общий
                    // конвеншен который может использоваться в иерархии с более чем одной имплементацией.
                }
            }
        }
    }
}
