package ru.vladislavsumin.core.coroutines.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.update

/**
 * Специальный тип [Flow] который можно прикрепить [Flow.linkTo] к другому [Flow].
 * Этот тип предназначен для разрыва зависимости на конструкторы, если необходимо организовать
 * дву направленную передачу данных между объектами.
 *
 * Обратите внимание, во избежание некорректного использования [LinkedFlow] может быть подписан только на один [Flow].
 * Попытка смены прикрепления вызовет ошибку.
 */
public class LinkedFlow<T> : Flow<T> {
    private val link = MutableStateFlow<Flow<T>?>(null)

    override suspend fun collect(collector: FlowCollector<T>) {
        link.filterNotNull().flatMapConcat { it }.collect(collector)
    }

    internal fun link(flow: Flow<T>) {
        link.update { old ->
            check(old == null) { "Link may be only one" }
            flow
        }
    }
}

/**
 * Прикрепляет [linkedFlow] к текущему [Flow]. После прикрепления все подписки на [linkedFlow]
 * будут передаваться этому [Flow].
 */
public fun <T> Flow<T>.linkTo(linkedFlow: LinkedFlow<T>) {
    linkedFlow.link(this)
}
