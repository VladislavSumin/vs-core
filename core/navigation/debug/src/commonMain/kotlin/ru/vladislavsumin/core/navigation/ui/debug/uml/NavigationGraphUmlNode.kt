package ru.vladislavsumin.core.navigation.ui.debug.uml

import ru.vladislavsumin.core.collections.tree.TreeNodeImpl
import ru.vladislavsumin.core.navigation.NavigationHost

public typealias NavigationGraphUmlNode = TreeNodeImpl<NavigationGraphUmlNodeInfo>

/**
 * @param name название параметров экрана.
 * @param hasDefaultParams есть ли у экрана параметры по умолчанию.
 * @param isPartOfMainGraph является ли эта нода частью навигационного графа.
 * @param description любое дополнительное описание на ваше усмотрение.
 * @param navigationHosts хосты навигации, обрабатываемые данным экраном
 */
public data class NavigationGraphUmlNodeInfo(
    val name: String,
    val hasDefaultParams: Boolean,
    val isPartOfMainGraph: Boolean,
    val description: String? = null,
    val navigationHosts: Set<NavigationHost> = emptySet(),
)
