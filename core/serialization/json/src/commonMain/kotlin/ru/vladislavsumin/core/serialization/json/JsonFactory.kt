package ru.vladislavsumin.core.serialization.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import ru.vladislavsumin.core.serialization.core.SerializersModulesRepository

internal interface JsonFactory {
    /**
     * Создает default [Json] инстанс. Добавляет в него [SerializersModule] объявленные в DI графе.
     */
    fun createDefault(): Json
}

internal class JsonFactoryImpl(
    private val serializersModulesRepository: SerializersModulesRepository,
) : JsonFactory {

    override fun createDefault() = Json {
        serializersModule = serializersModulesRepository.serializerModule
    }
}
