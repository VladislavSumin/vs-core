package ru.vladislavsumin.convention.test

import ru.vladislavsumin.utils.protectFromDslAccessors
import ru.vladislavsumin.utils.vsCoreLibs

/**
 * Подключает [roborazzi](https://github.com/takahirom/roborazzi) для скриншот тестирования compose контента.
 *
 * Roborazzi рендерит compose через тот же skiko что и desktop таргет и сравнивает результат с эталоном попиксельно.
 * Плагин создает gradle таски `recordRoborazziJvm` / `compareRoborazziJvm` / `verifyRoborazziJvm`.
 *
 * Зависимость [roborazzi-compose-desktop] добавляется только в jvmTest, так как это desktop (skiko) артефакт и он
 * не может быть подключен в commonTest full KMP модулей (сломает компиляцию не jvm таргетов).
 *
 * Модуль так же должен применять [ru.vladislavsumin.convention.compose] для доступа к `compose.uiTest`.
 */

plugins {
    id("ru.vladislavsumin.convention.kmp.common")
    id("io.github.takahirom.roborazzi")
}

protectFromDslAccessors {
    kotlin {
        sourceSets {
            jvmTest {
                dependencies {
                    implementation(vsCoreLibs.testing.roborazzi.composeDesktop)
                }
            }
        }
    }
}
