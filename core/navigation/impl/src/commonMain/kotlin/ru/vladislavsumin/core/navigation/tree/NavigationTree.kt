package ru.vladislavsumin.core.navigation.tree

import ru.vladislavsumin.core.collections.tree.LinkedTreeNode
import ru.vladislavsumin.core.navigation.InternalNavigationApi

/**
 * Главное древо навигации, описывает связи между экранами, то, какие экраны открывают внутри себя другие экраны.
 */
@InternalNavigationApi
public class NavigationTree(root: LinkedTreeNode<ScreenInfo>) : LinkedTreeNode<ScreenInfo> by root
