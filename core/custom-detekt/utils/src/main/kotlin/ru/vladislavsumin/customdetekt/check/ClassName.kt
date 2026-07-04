package ru.vladislavsumin.customdetekt.check

import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtClass

fun Rule.checkName(klass: KtClass, regex: Regex, message: () -> String) {
    val name = klass.name
    if (name == null || !regex.matches(name)) {
        report(
            Finding(
                entity = Entity.from(klass.nameIdentifier!!),
                message = message(),
            ),
        )
    }
}
