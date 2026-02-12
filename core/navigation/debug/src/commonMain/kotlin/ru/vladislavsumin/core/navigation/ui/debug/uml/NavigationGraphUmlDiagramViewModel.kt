package ru.vladislavsumin.core.navigation.ui.debug.uml

import ru.vladislavsumin.core.collections.tree.LinkedTreeNode
import ru.vladislavsumin.core.collections.tree.TreeNodeImpl
import ru.vladislavsumin.core.collections.tree.map
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.navigation.GenericNavigation
import ru.vladislavsumin.core.navigation.tree.ScreenInfo

internal class NavigationGraphUmlDiagramViewModelFactory(
    private val navigationProvider: () -> GenericNavigation<*>,
) {
    fun create(
        navigationTreeInterceptor: NavigationTreeInterceptor,
    ): NavigationGraphUmlDiagramViewModel {
        return NavigationGraphUmlDiagramViewModel(navigationProvider(), navigationTreeInterceptor)
    }
}

internal class NavigationGraphUmlDiagramViewModel(
    private val navigation: GenericNavigation<*>,
    private val navigationTreeInterceptor: NavigationTreeInterceptor,
) : ViewModel() {

    val graph = createDebugGraph()

    private fun createDebugGraph() = NavigationGraphUmlDiagramViewState(
        root = navigationTreeInterceptor(
            mapNodesRecursively(node = navigation.navigationTree as LinkedTreeNode<ScreenInfo<*>>),
        ),
    )

    /**
     * Переводит все [NavigationTree.Node] исходного графа навигации в граф [NavigationGraphUmlDiagramViewState.Node].
     */
    private fun mapNodesRecursively(node: LinkedTreeNode<ScreenInfo<*>>): TreeNodeImpl<InternalNavigationGraphUmlNode> {
        return node.map {
            InternalNavigationGraphUmlNode(
                name = it.screenKey.key.simpleName!!,
                hasDefaultParams = it.defaultParams != null,
                description = it.description,
                navigationHosts = it.navigationHosts,
            )
        }
    }
}
