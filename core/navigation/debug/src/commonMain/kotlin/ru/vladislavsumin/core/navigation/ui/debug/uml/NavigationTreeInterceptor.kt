package ru.vladislavsumin.core.navigation.ui.debug.uml

import ru.vladislavsumin.core.collections.tree.TreeNodeImpl

public fun interface NavigationTreeInterceptor {
    public operator fun invoke(
        node: TreeNodeImpl<InternalNavigationGraphUmlNode>,
    ): TreeNodeImpl<out NavigationGraphUmlNode>
}
