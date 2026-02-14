package ru.vladislavsumin.convention.publication

import ru.vladislavsumin.configuration.projectConfiguration
import ru.vladislavsumin.utils.fullName

/**
 * Устанавливает группу проекта основываясь на его пути
 */

val subgroup = project.parent?.let { ".${it.fullName()}" } ?: ""
group = "${project.projectConfiguration.basePackage}$subgroup"
