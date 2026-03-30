package ru.vladislavsumin.core.session.client

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import ru.vladislavsumin.core.logger.api.logger
import ru.vladislavsumin.core.logger.common.Logger

public interface SessionClient<S, R> {
    public val connection: Flow<ConnectionState<S, R>>

    public sealed interface ConnectionState<S, R> {
        public class Disconnected<S, R> : ConnectionState<S, R>
        public class Connecting<S, R> : ConnectionState<S, R>
        public data class Connected<S, R>(
            val send: suspend (message: S) -> Unit,
            val incoming: Flow<R>,
        ) : ConnectionState<S, R>
    }
}

public fun <S, R> SessionClient(
    connector: SessionConnector<S, R>,
    scope: CoroutineScope,
    allowConnection: Flow<Boolean> = flowOf(true),
    logger: Logger = logger("VsSession"),
): SessionClient<S, R> = SessionClientImpl(connector, scope, allowConnection, logger)

internal class SessionClientImpl<S, R>(
    private val connector: SessionConnector<S, R>,
    scope: CoroutineScope,
    allowConnection: Flow<Boolean>,
    private val logger: Logger,
) : SessionClient<S, R> {
    override val connection: Flow<SessionClient.ConnectionState<S, R>> = allowConnection
        .distinctUntilChanged()
        .flatMapLatest { allowConnection ->
            if (allowConnection) {
                createConnectionFlow()
            } else {
                flowOf(SessionClient.ConnectionState.Disconnected())
            }
        }
        .shareIn(
            scope + CoroutineName("VsSession::connection"),
            SharingStarted.WhileSubscribed(
                stopTimeoutMillis = 0, //connectionKeepAliveTime,
                replayExpirationMillis = 0,
            ),
            1,
        )

    private fun createConnectionFlow(): Flow<SessionClient.ConnectionState<S, R>> = channelFlow {
        logger.d { "Start observe connection" }

        // Holder для SessionConnection
        // В случае любой ошибки, или отмены скоупа мы должны вызвать SessionConnection.close для корректного
        // закрытия соединения.
        var connectionGlobal: SessionConnection<S, R>? = null

        // Глобальный try-cath блок, нужен, что бы корректно закрыть connectionGlobal в любом случае.
        try {
            send(SessionClient.ConnectionState.Connecting())

            while (true) {
                try {
                    // this cope prevents reconnect before old connection receive flow not closed
                    // see [crateConnectedState] function
                    coroutineScope {
                        // Establish connection
                        val connection = connector.connect()
                        connectionGlobal = connection

                        // Create connected state wrapper around connection
                        val state = createConnectedState(connection, this)
                        send(state)
                    }
                } catch (e: Exception) {
                    // TODO обработка ошибок
                    throw e
                }
            }
        } finally {
            logger.d { "Stopping observe connection" }
            withContext(NonCancellable) {
                connectionGlobal?.close()
                logger.d { "Stop observe connection" }
            }
        }
    }
        .distinctUntilChanged()

    private fun createConnectedState(
        connection: SessionConnection<S, R>,
        scope: CoroutineScope,
    ): SessionClient.ConnectionState.Connected<S, R> {
        return SessionClient.ConnectionState.Connected(
            send = { connection.send.send(it) },
            incoming = connection.receive
                .receiveAsFlow()
                .onEach { logger.t { "Received message: $it" } }
                // Hot observable, subscribe immediately, shared, no buffer, connection scoped
                .shareIn(scope, SharingStarted.Eagerly),
        )
    }
}