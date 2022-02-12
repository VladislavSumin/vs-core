package ru.vs.iot.ktor_client.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.serialization.json.Json
import org.kodein.di.DirectDI
import org.kodein.di.instance

private class AndroidHttpClientFactory(private val json: Json) : HttpClientFactory {
    override fun createHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(JsonFeature) {
                serializer = KotlinxSerializer(json)
            }
        }
    }
}

internal actual fun DirectDI.createHttpClientFactory(): HttpClientFactory {
    return AndroidHttpClientFactory(instance())
}
