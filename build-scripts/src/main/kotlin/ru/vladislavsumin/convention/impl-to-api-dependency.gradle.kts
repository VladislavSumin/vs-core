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
                check(project.name.endsWith(suffix)) { "impl-to-api-dependency can apply only to impl modules" }
                val apiProjectName = project.name.dropLast(suffix.length) + "api"
                val apiProject = project.parent!!.findProject(apiProjectName)
                checkNotNull(apiProject) { "api project $apiProjectName not found for project ${project.name}" }
                api(apiProject)
            }
        }
    }
}
