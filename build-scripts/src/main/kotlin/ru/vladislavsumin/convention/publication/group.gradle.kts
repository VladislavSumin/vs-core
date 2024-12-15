package ru.vladislavsumin.convention.publication

import ru.vladislavsumin.configuration.projectConfiguration
import ru.vladislavsumin.utils.fullName

/**
 * Устанавливает группу проекта основываясь на его пути
 */

group = "${project.projectConfiguration.basePackage}.${project.parent!!.fullName()}"
