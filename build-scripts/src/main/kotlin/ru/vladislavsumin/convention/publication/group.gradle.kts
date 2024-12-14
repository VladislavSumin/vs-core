package ru.vladislavsumin.convention.publication

import ru.vladislavsumin.utils.fullName

/**
 * Устанавливает группу проекта основываясь на его пути
 */

group = "ru.vladislavsumin.${project.parent!!.fullName()}"
