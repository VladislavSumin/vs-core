package ru.vladislavsumin.utils

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

/**
 * Предоставляет доступ к каталогу версий внутри convention плагинов и прочего кода.
 */
val Project.libs: LibrariesForLibs get() = rootProject.the()