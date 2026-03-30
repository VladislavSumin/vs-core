package ru.vladislavsumin.core.session.connector.socket.client

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import ru.vladislavsumin.core.session.client.SessionConnection
import ru.vladislavsumin.core.session.client.SessionConnector

public class SocketSessionConnector<S, R>(
    private val socketFactory: suspend () -> Socket,
    private val packetReader: suspend BufferedInputStream.() -> R,
    private val packetWriter: suspend BufferedOutputStream.(packet: S) -> Unit,
) : SessionConnector<S, R> {
    override suspend fun connect(workerScope: CoroutineScope): SessionConnection<S, R> {
        val socket = socketFactory()
        val output = socket.getOutputStream().buffered()
        val input = socket.getInputStream().buffered()

        val receiveChannel = Channel<R>()
        val sendChannel = Channel<S>()

        workerScope.launch {
            while (true) {
                val packet = packetReader(input)
                receiveChannel.send(packet)
            }
        }

        workerScope.launch {
            for (packet in sendChannel) {
                packetWriter(output, packet)
            }
        }

        return object : SessionConnection<S, R> {
            override val receive: ReceiveChannel<R> = receiveChannel
            override val send: SendChannel<S> = sendChannel

            override suspend fun close() {
                output.close()
                input.close()
                socket.close()
            }
        }
    }
}
