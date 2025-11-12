package ru.vladislavsumin.core.navigation.ui.debug.uml

import com.arkivanov.decompose.GenericComponentContext
import ru.vladislavsumin.core.collections.tree.LinkedTreeNode
import ru.vladislavsumin.core.collections.tree.map
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.navigation.GenericNavigation
import ru.vladislavsumin.core.navigation.screen.GenericComposeScreen
import ru.vladislavsumin.core.navigation.tree.ScreenInfo

internal class NavigationGraphUmlDiagramViewModelFactory<Ctx : GenericComponentContext<Ctx>>(
    private val navigationProvider: () -> GenericNavigation<*, GenericComposeScreen<Ctx>>,
) {
    fun create(
        navigationTreeInterceptor: (NavigationGraphUmlNode) -> NavigationGraphUmlNode,
    ): NavigationGraphUmlDiagramViewModel<Ctx> {
        return NavigationGraphUmlDiagramViewModel(navigationProvider(), navigationTreeInterceptor)
    }
}

internal class NavigationGraphUmlDiagramViewModel<Ctx : GenericComponentContext<Ctx>>(
    private val navigation: GenericNavigation<*, GenericComposeScreen<Ctx>>,
    private val navigationTreeInterceptor: (NavigationGraphUmlNode) -> NavigationGraphUmlNode,
) : ViewModel() {

    val graph = createDebugGraph()

    private fun createDebugGraph(): NavigationGraphUmlDiagramViewState {
        return NavigationGraphUmlDiagramViewState(
            root = navigationTreeInterceptor(
                mapNodesRecursively(
                    navigation.navigationTree as LinkedTreeNode<ScreenInfo<*, GenericComposeScreen<Ctx>>>,
                ),
            ),
        )
    }

    /**
     * Переводит все [NavigationTree.Node] исходного графа навигации в граф [NavigationGraphUmlDiagramViewState.Node].
     */
    private fun mapNodesRecursively(
        node: LinkedTreeNode<ScreenInfo<*, GenericComposeScreen<Ctx>>>,
    ): NavigationGraphUmlNode {
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
