package ru.vladislavsumin.core.session.client

/**
 * Реализует создание [SessionConnection].
 */
public interface SessionConnector<S, R> {
    public suspend fun connect(): SessionConnection<S, R>
}
