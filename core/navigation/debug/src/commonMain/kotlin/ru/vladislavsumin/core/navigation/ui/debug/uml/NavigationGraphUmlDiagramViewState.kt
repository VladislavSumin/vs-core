package ru.vladislavsumin.core.navigation.ui.debug.uml

import androidx.compose.runtime.Stable
import ru.vladislavsumin.core.collections.tree.TreeNodeImpl

@Stable
internal data class NavigationGraphUmlDiagramViewState(
    val root: TreeNodeImpl<out NavigationGraphUmlNode>,
)
