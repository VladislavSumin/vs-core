package ru.vladislavsumin.core.decompose.components

import com.arkivanov.essenty.statekeeper.StateKeeper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Реализация паттерна MVVM, похожа на ViewModel от Google, но использует механизмы lifecycle из decompose.
 *
 * Так же обладает полезными функциями-расширениями для удобного решения типовых задач встречающихся в viewModel.
 */
public abstract class ViewModel {
    /**
     * [CoroutineScope] с viewmodel lifecycle.
     */
    protected val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    @PublishedApi
    internal val stateKeeper: StateKeeper = let {
        val keeper = WhileConstructedViewModelStateKeeper ?: throw WrongViewModelUsageException()
        WhileConstructedViewModelStateKeeper = null
        keeper
    }

    /**
     * Укороченная версия [stateIn] с использованием [viewModelScope] и [SharingStarted.Eagerly] по умолчанию.
     */
    protected fun <T> Flow<T>.stateIn(
        initialValue: T,
        started: SharingStarted = SharingStarted.Eagerly,
    ): StateFlow<T> {
        return stateIn(
            scope = viewModelScope,
            started = started,
            initialValue = initialValue,
        )
    }

    /**
     * Укороченная версия [CoroutineScope.launch] использующая в качестве скоупа [viewModelScope].
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
        viewModelScope.launch(context, start, block)
    }

    /**
     * Создает [StateFlow], данные в котором могут переживать смерть процесса через механизм [stateKeeper] экрана.
     *
     * @param key уникальный в рамках [ViewModel] ключ, по которому будут сохраняться данные.
     * @param initialValue фабрика для создания инициирующего значения если нет сохраненного значения.
     */
    protected inline fun <reified T : Any> saveableStateFlow(
        key: String,
        initialValue: () -> T,
    ): MutableStateFlow<T> {
        val serializer = Json.serializersModule.serializer<T>()
        val savedState = stateKeeper.consume<T>(key, serializer)
        val flow = MutableStateFlow(savedState ?: initialValue())
        stateKeeper.register(key, serializer) { flow.value }
        return flow
    }

    protected inline fun <reified T : Any> saveableStateFlow(
        key: String,
        initialValue: T,
    ): MutableStateFlow<T> = saveableStateFlow(key) { initialValue }

    /**
     * Вызывается при уничтожении экземпляра [ViewModel]. Закрывает [CoroutineScope].
     *
     * Данный метод предназначен только для внутреннего использования, если вам нужно завершить какие-либо процессы при
     * уничтожении [ViewModel], то используйте для этой задачи факт отмены [viewModelScope].
     */
    internal fun onDestroy() {
        viewModelScope.cancel()
    }
}

internal class WrongViewModelUsageException : Exception(
    """Wrong ViewModel usage. 
        |ViewModel creation allowed only inside view model function in Component class. 
        |Only ONE view model can be create inside viewModel function at same time"""
        .trimMargin(),
)
