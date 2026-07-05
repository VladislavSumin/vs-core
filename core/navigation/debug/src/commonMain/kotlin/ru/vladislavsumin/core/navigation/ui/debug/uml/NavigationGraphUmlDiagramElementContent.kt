package ru.vladislavsumin.core.navigation.ui.debug.uml

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.navigation.NavigationHost

/**
 * Заголовок ноды навигационного графа: имя экрана и дополнительная информация о нём.
 *
 * Внешняя рамка (карточка) и хосты навигации рисуются отдельно средствами [NavigationGraphNode], так как их
 * положение вычисляется на этапе measure/layout для корректной привязки соединительных линий к конкретным хостам.
 */
@Composable
internal fun NavigationNodeHeader(info: NavigationGraphUmlNode, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = info.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        if (info is InternalNavigationGraphUmlNode && !info.hasDefaultParams) {
            Text(
                "hasDefaultParams=${info.hasDefaultParams}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (info.description != null) {
            Text(
                info.description!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

/**
 * Небольшая карточка одного [NavigationHost], располагается внутри карточки родительской ноды.
 */
@Composable
internal fun NavigationHostCard(navigationHost: NavigationHost, modifier: Modifier = Modifier) {
    Card(modifier) {
        Text(
            text = navigationHost::class.simpleName ?: "NoName",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(
                    horizontal = 8.dp,
                    vertical = 4.dp,
                ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

internal val NavigationGraphUmlNode.isPartOfMainGraph: Boolean
    get() = when (this) {
        is ExternalNavigationGraphUmlNode -> false
        is InternalNavigationGraphUmlNode -> true
    }
