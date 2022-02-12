package ru.vs.iot.ktor_client.api

import io.ktor.client.HttpClient
import org.kodein.di.DirectDI

interface HttpClientFactory {
    fun createHttpClient(): HttpClient
}

internal expect fun DirectDI.createHttpClientFactory(): HttpClientFactory
