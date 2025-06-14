package ru.vladislavsumin.core.navigation.ui.debug.uml

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.navigation.Navigation

/**
 * @param navigationProvider провайдер [Navigation] на основе которой будет построен граф. Выполнен в виде
 * провайдера, так как данная фабрика может встраиваться в один из экранов собственно навигации и для избежания
 * зацикливания DI выбран именно такой способ предоставления зависимостей.
 */
public class NavigationGraphUmlDiagramComponentFactory(
    navigationProvider: () -> Navigation,
) {
    private val viewModelFactory = NavigationGraphUmlDiagramViewModelFactory(navigationProvider)

    /**
     * @param navigationTreeInterceptor перехватывает ноды созданные из графа навигации и позволяет внести в полученный
     * граф любые изменения, например добавить экраны инициализации, которые не являются частью графа. **Внимание** этот
     * параметр передается в viewModel и имеет отличный от компонента lifecycle.
     */
    public fun create(
        context: ComponentContext,
        navigationTreeInterceptor: (NavigationGraphUmlNode) -> NavigationGraphUmlNode = { it },
    ): ComposeComponent {
        return NavigationGraphUmlDiagramComponent(
            viewModelFactory,
            context,
            navigationTreeInterceptor,
        )
    }
}

/**
 * Отображает текущий граф навигации в удобной для человека форме.
 */
internal class NavigationGraphUmlDiagramComponent(
    viewModelFactory: NavigationGraphUmlDiagramViewModelFactory,
    context: ComponentContext,
    navigationTreeInterceptor: (NavigationGraphUmlNode) -> NavigationGraphUmlNode,
) : Component(context), ComposeComponent {
    private val viewModel: NavigationGraphUmlDiagramViewModel = viewModel {
        viewModelFactory.create(navigationTreeInterceptor)
    }

    @Composable
    override fun Render(modifier: Modifier) = NavigationGraphUmlDiagramContent(viewModel, modifier)
}
