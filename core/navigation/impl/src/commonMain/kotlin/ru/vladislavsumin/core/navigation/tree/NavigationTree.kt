package ru.vladislavsumin.core.navigation.tree

import com.arkivanov.decompose.GenericComponentContext
import ru.vladislavsumin.core.collections.tree.LinkedTreeNode
import ru.vladislavsumin.core.navigation.InternalNavigationApi
import ru.vladislavsumin.core.navigation.screen.Render

/**
 * Главное древо навигации, описывает связи между экранами, то, какие экраны открывают внутри себя другие экраны.
 */
@InternalNavigationApi
public class NavigationTree<Ctx : GenericComponentContext<Ctx>, R : Render>(
    root: LinkedTreeNode<ScreenInfo<Ctx, R>>,
) :
    LinkedTreeNode<ScreenInfo<Ctx, R>> by root
