package ru.vladislavsumin.core.navigation.ui.debug.uml

import ru.vladislavsumin.core.navigation.NavigationHost

/**
 * Ui представление одной ноды навигационного графа.
 */
public sealed interface NavigationGraphUmlNode {
    public val name: String
    public val description: String?
}

/**
 * Внешняя нода не являющаяся частью фреймворка навигации
 */
public data class ExternalNavigationGraphUmlNode(override val name: String, override val description: String? = null) :
    NavigationGraphUmlNode

/**
 * Внутренняя нода являющаяся частью графа навигации.
 *
 * @param name название параметров экрана.
 * @param hasDefaultParams есть ли у экрана параметры по умолчанию.
 * @param description любое дополнительное описание на ваше усмотрение.
 * @param navigationHosts хосты навигации, обрабатываемые данным экраном
 * @param hostInParent хост родительского экрана, внутри которого располагается этот экран, null для рут экрана
 * или для нод, добавленных вне графа навигации.
 */
@ConsistentCopyVisibility
public data class InternalNavigationGraphUmlNode internal constructor(
    override val name: String,
    override val description: String? = null,
    internal val hasDefaultParams: Boolean,
    internal val navigationHosts: Set<NavigationHost> = emptySet(),
    internal val hostInParent: NavigationHost? = null,
) : NavigationGraphUmlNode {

    /**
     * Позволяет менять внешние параметры ноды
     */
    public fun externalCopy(
        name: String = this.name,
        description: String? = this.description,
    ): InternalNavigationGraphUmlNode = copy(name = name, description = description)
}
