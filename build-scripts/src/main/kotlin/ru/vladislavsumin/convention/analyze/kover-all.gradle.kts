package ru.vladislavsumin.convention.analyze

import ru.vladislavsumin.utils.protectFromDslAccessors

/**
 * Настройка kover плагина по умолчанию для всех модулей + комбинированный отчет.
 */

plugins {
    id("org.jetbrains.kotlinx.kover")
}

check(project === rootProject) { "This convention may be applied only to root project" }

allprojects {
    apply {
        plugin("ru.vladislavsumin.convention.analyze.kover")
    }
}

protectFromDslAccessors {
    kover {
        reports {
            total {
                xml {
                    onCheck = false
                }
                html {
                    onCheck = false
                }
            }
        }
    }

    dependencies {
        subprojects.forEach { subproject ->
            kover(subproject)
        }
    }
}
