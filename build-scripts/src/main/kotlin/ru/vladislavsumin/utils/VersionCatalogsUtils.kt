package ru.vladislavsumin.utils

import org.gradle.accessors.dm.LibrariesForVsCoreLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

/**
 * Предоставляет доступ к каталогу версий внутри convention плагинов и прочего кода.
 */
val Project.vsCoreLibs: LibrariesForVsCoreLibs get() = rootProject.the()
