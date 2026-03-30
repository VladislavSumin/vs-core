package ru.vladislavsumin.core.session.client

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

/**
 * Представление одного соединения с сервером.
 *
 * @param S тип передаваемых сообщений.
 * @param R тип принимаемых сообщений.
 */
public interface SessionConnection<S, R> {
    public val receive: ReceiveChannel<R>
    public val send: SendChannel<S>
    public suspend fun close()
}