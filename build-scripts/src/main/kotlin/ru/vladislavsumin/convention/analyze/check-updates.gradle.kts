package ru.vladislavsumin.convention.analyze

/**
 * Базовая настройка плагина для проверки версий библиотек
 */

plugins {
    id("com.github.ben-manes.versions")
}

tasks.dependencyUpdates.configure {
    fun isNonStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(version)
        return !isStable
    }
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}
