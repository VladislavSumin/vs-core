package ru.vladislavsumin.core.navigation.tree

import ru.vladislavsumin.core.collections.tree.LinkedTreeNode

/**
 * Главное древо навигации, описывает связи между экранами, то какие экраны открывают внутри себя другие экраны.
 *
 * @param repository репозиторий с исходными данными для построения дерева.
 */

internal class NavigationTree(root: LinkedTreeNode<ScreenInfo>) : LinkedTreeNode<ScreenInfo> by root
