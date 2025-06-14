package ru.vladislavsumin.core.decompose.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.vladislavsumin.core.decompose.components.utils.asValue
import ru.vladislavsumin.core.decompose.components.utils.createCoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Базовая реализация компонента с набором полезных расширений.
 */
public abstract class Component(context: ComponentContext) : ComponentContext by context {
    protected val scope: CoroutineScope = lifecycle.createCoroutineScope()

    /**
     * Укороченная версия [CoroutineScope.launch] использующая в качестве скоупа [scope].
     *
     * Так же отличается тем что возвращает [Unit] вместо [Job] для возможности использования короткого синтаксиса:
     * ```
     * fun sampleFunction() = launch { ... }
     * ```
     */
    protected fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit,
    ) {
        scope.launch(context, start, block)
    }

    /**
     * Подписка на [StateFlow] через [asValue] с использованием локального scope компонента.
     */
    protected fun <T : Any> StateFlow<T>.asValue(): Value<T> = asValue(scope)

    /**
     * Создает или возвращает созданную ранее [ViewModel] используя для этого [instanceKeeper].
     */
    protected open fun <T : ViewModel> viewModel(factory: () -> T): T {
        // Мы не можем использовать inline тут, так как хотим предоставить возможность переопределения
        // этой функции в наследниках, в таких условиях использования класса лямбды фабрики кажется адекватным решением.
        val key = factory::class.toString()

        val viewModelHolder = instanceKeeper.getOrCreate(key) {
            // Создаем отдельный state keeper для вью модели, вспоминать сохраненные данные он будет только при создании
            // модели, но это ожидаемое поведение, при пересоздании экрана с сохранением модели перезагружать данные
            // не нужно.
            val state = stateKeeper.consume(key, SerializableContainer.serializer())
            val viewModelStateKeeperDispatcher = StateKeeperDispatcher(state)

            val viewModel = try {
                WhileConstructedViewModelStateKeeper = viewModelStateKeeperDispatcher
                factory()
            } finally {
                WhileConstructedViewModelStateKeeper = null
            }

            ViewModelHolder(viewModel, viewModelStateKeeperDispatcher)
        }

        // В отличие от кейса восстановления данных, сохранять данные вью модели нужно при сохранении данных экрана,
        // так как мы никогда не можем быть уверены, что даже если экран сохраняется перед пересозданием он фактически
        // будет пересоздан. Поэтому во избежание, сохраняем всегда.
        stateKeeper.register(key, SerializableContainer.serializer()) { viewModelHolder.viewModelStateKeeper.save() }
        return viewModelHolder.viewModel
    }
}
