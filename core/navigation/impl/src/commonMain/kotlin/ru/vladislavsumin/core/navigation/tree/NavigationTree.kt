package ru.vladislavsumin.core.navigation.tree

import com.arkivanov.decompose.GenericComponentContext
import ru.vladislavsumin.core.collections.tree.LinkedTreeNode
import ru.vladislavsumin.core.navigation.InternalNavigationApi
import ru.vladislavsumin.core.navigation.screen.GenericScreen

/**
 * Главное древо навигации, описывает связи между экранами, то, какие экраны открывают внутри себя другие экраны.
 */
@InternalNavigationApi
public class NavigationTree<Ctx : GenericComponentContext<Ctx>, BS : GenericScreen<Ctx, BS>>(
    root: LinkedTreeNode<ScreenInfo<Ctx, BS>>,
) :
    LinkedTreeNode<ScreenInfo<Ctx, BS>> by root
