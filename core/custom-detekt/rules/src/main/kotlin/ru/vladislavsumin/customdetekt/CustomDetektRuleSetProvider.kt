package ru.vladislavsumin.customdetekt

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider
import ru.vladislavsumin.customdetekt.rules.ModuleRootPackage

class CustomDetektRuleSetProvider : RuleSetProvider {

    override val ruleSetId: RuleSetId = RuleSetId("custom")

    override fun instance(): RuleSet = RuleSet(
        ruleSetId,
        listOf(::ModuleRootPackage),
    )
}
