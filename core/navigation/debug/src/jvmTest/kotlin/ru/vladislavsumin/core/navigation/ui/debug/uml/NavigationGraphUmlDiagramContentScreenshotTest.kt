package ru.vladislavsumin.core.navigation.ui.debug.uml

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.collections.tree.TreeNodeImpl
import ru.vladislavsumin.core.collections.tree.nodeOf
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.uikit.screenshot.screenshotTest
import kotlin.test.Test

internal class NavigationGraphUmlDiagramContentScreenshotTest {

    /**
     * Одна нода — граф из единственного экрана.
     */
    @Test
    fun singleNode() {
        screenshot("uml_single_node", DpSize(240.dp, 140.dp)) {
            node(internalNode("RootScreen"))
        }
    }

    /**
     * Ветвление — корень с несколькими дочерними экранами.
     */
    @Test
    fun branchingGraph() {
        screenshot("uml_branching_graph", DpSize(520.dp, 240.dp)) {
            node(
                internalNode("Root"),
                node(internalNode("Home")),
                node(internalNode("Profile")),
                node(internalNode("Settings")),
            )
        }
    }

    /**
     * Многоуровневый граф с вложенностью.
     */
    @Test
    fun deepGraph() {
        screenshot("uml_deep_graph", DpSize(560.dp, 380.dp)) {
            node(
                internalNode("Root"),
                node(
                    internalNode("Main"),
                    node(internalNode("Feed")),
                    node(internalNode("Search")),
                ),
                node(
                    internalNode("Auth"),
                    node(internalNode("Login")),
                ),
            )
        }
    }

    /**
     * Нода с расширенным описанием: без параметров по умолчанию, с описанием и хостами навигации.
     */
    @Test
    fun nodeWithDetails() {
        screenshot("uml_node_with_details", DpSize(440.dp, 320.dp)) {
            node(
                internalNode(
                    name = "Dashboard",
                    hasDefaultParams = false,
                    description = "Стартовый экран",
                    navigationHosts = setOf(RootHost, ModalHost),
                ),
                node(internalNode("Details", description = "Детали экрана", hostInParent = RootHost)),
            )
        }
    }

    /**
     * Дочерние экраны разделены по принадлежности к хостам навигации: листья одного хоста укладываются
     * компактно вертикально, а ветвящийся дочерний экран другого хоста — горизонтально.
     */
    @Test
    fun hostGroupedGraph() {
        screenshot("uml_host_grouped_graph", DpSize(680.dp, 460.dp)) {
            node(
                internalNode("Root", navigationHosts = setOf(RootHost, ModalHost)),
                node(internalNode("Home", hostInParent = RootHost)),
                node(internalNode("Profile", hostInParent = RootHost)),
                node(internalNode("Settings", hostInParent = RootHost)),
                node(
                    internalNode("Dialog", navigationHosts = setOf(DialogHost), hostInParent = ModalHost),
                    node(internalNode("Confirm", hostInParent = DialogHost)),
                ),
            )
        }
    }

    /**
     * Граф с внешней (не являющейся частью фреймворка) нодой, отрисованной пунктиром.
     */
    @Test
    fun withExternalNode() {
        screenshot("uml_with_external_node", DpSize(520.dp, 240.dp)) {
            node(
                internalNode("Root"),
                node(ExternalNavigationGraphUmlNode(name = "DeepLink", description = "внешний вход")),
                node(internalNode("Content")),
            )
        }
    }

    private fun screenshot(goldenName: String, size: DpSize, graph: () -> TreeNodeImpl<NavigationGraphUmlNode>) {
        val root = graph()
        screenshotTest(goldenName = goldenName, size = size) {
            Content(root)
        }
    }

    @Composable
    private fun Content(root: TreeNodeImpl<NavigationGraphUmlNode>) {
        MaterialTheme {
            Surface(Modifier.fillMaxSize()) {
                NavigationGraphUmlDiagramContent(
                    root = root,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    private fun node(
        value: NavigationGraphUmlNode,
        vararg children: TreeNodeImpl<NavigationGraphUmlNode>,
    ): TreeNodeImpl<NavigationGraphUmlNode> = nodeOf(value, *children)

    private fun internalNode(
        name: String,
        hasDefaultParams: Boolean = true,
        description: String? = null,
        navigationHosts: Set<NavigationHost> = emptySet(),
        hostInParent: NavigationHost? = null,
    ): NavigationGraphUmlNode = InternalNavigationGraphUmlNode(
        name = name,
        description = description,
        hasDefaultParams = hasDefaultParams,
        navigationHosts = navigationHosts,
        hostInParent = hostInParent,
    )

    private object RootHost : NavigationHost
    private object ModalHost : NavigationHost
    private object DialogHost : NavigationHost
}
