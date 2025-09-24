package ru.vladislavsumin.core.navigation.ui.debug.uml

import ru.vladislavsumin.core.collections.tree.LinkedTreeNode
import ru.vladislavsumin.core.collections.tree.map
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.navigation.GenericNavigation
import ru.vladislavsumin.core.navigation.screen.ComposeRender
import ru.vladislavsumin.core.navigation.tree.ScreenInfo

internal class NavigationGraphUmlDiagramViewModelFactory(
    private val navigationProvider: () -> GenericNavigation<*, ComposeRender>,
) {
    fun create(
        navigationTreeInterceptor: (NavigationGraphUmlNode) -> NavigationGraphUmlNode,
    ): NavigationGraphUmlDiagramViewModel {
        return NavigationGraphUmlDiagramViewModel(navigationProvider(), navigationTreeInterceptor)
    }
}

internal class NavigationGraphUmlDiagramViewModel(
    private val navigation: GenericNavigation<*, ComposeRender>,
    private val navigationTreeInterceptor: (NavigationGraphUmlNode) -> NavigationGraphUmlNode,
) : ViewModel() {

    val graph = createDebugGraph()

    private fun createDebugGraph(): NavigationGraphUmlDiagramViewState {
        return NavigationGraphUmlDiagramViewState(
            root = navigationTreeInterceptor(
                mapNodesRecursively(
                    navigation.navigationTree as LinkedTreeNode<ScreenInfo<*, ComposeRender>>,
                ),
            ),
        )
    }

    /**
     * Переводит все [NavigationTree.Node] исходного графа навигации в граф [NavigationGraphUmlDiagramViewState.Node].
     */
    private fun mapNodesRecursively(node: LinkedTreeNode<ScreenInfo<*, ComposeRender>>): NavigationGraphUmlNode {
        return node.map {
            NavigationGraphUmlNodeInfo(
                name = it.screenKey.key.simpleName!!,
                hasDefaultParams = it.defaultParams != null,
                isPartOfMainGraph = true,
                description = it.description,
                navigationHosts = it.navigationHosts,
            )
        }
    }
}
