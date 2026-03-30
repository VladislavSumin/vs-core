package ru.vladislavsumin.core.session.client

import kotlinx.coroutines.CoroutineScope

/**
 * Реализует создание [SessionConnection].
 */
public interface SessionConnector<S, R> {
    /**
     * @param workerScope специальный скоуп на котором можно запустить задачи по работе с подключением.
     * Они будут автоматически закрыты при закрытии соединения, так же как и ошибки в этом скоупе приведут
     * к закрытию соединения с ошибкой.
     */
    public suspend fun connect(workerScope: CoroutineScope): SessionConnection<S, R>
}
