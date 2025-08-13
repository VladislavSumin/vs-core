package ru.vladislavsumin.core.navigation.tree

import com.arkivanov.decompose.GenericComponentContext
import ru.vladislavsumin.core.collections.tree.LinkedTreeNode
import ru.vladislavsumin.core.navigation.InternalNavigationApi

/**
 * Главное древо навигации, описывает связи между экранами, то, какие экраны открывают внутри себя другие экраны.
 */
@InternalNavigationApi
public class NavigationTree<Ctx : GenericComponentContext<Ctx>>(root: LinkedTreeNode<ScreenInfo<Ctx>>) :
    LinkedTreeNode<ScreenInfo<Ctx>> by root
