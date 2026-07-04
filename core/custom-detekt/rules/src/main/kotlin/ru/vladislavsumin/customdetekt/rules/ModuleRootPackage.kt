package ru.vladislavsumin.customdetekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtPackageDirective
import java.net.URI

class ModuleRootPackage(config: Config = Config.empty) :
    Rule(
        config = config,
        description = "Проверка что пакет файла начинается с базового пакета проекта",
        url = URI(""),
    ) {

    override fun visitPackageDirective(directive: KtPackageDirective) {
        super.visitPackageDirective(directive)

        val basePackage = config.valueOrDefault("basePackage", "")
        if (basePackage.isBlank()) return

        val filePackage = directive.fqName
        if (filePackage.isRoot) return

        val expected = FqName(basePackage)
        if (!filePackage.startsWith(expected)) {
            report(
                Finding(
                    entity = Entity.from(directive),
                    message = "Пакет должен начинаться с ${expected.asString()}",
                ),
            )
        }
    }
}
