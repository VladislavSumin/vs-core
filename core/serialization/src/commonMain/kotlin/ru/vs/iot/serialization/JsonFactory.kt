package ru.vs.iot.serialization

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus

interface JsonFactory {
    fun createDefault(): Json
}

internal class JsonFactoryImpl(
    private val serializersModulesSet: Set<SerializersModule>
) : JsonFactory {

    private val mergedModule by lazy {
        serializersModulesSet.fold(null as SerializersModule?) { m1, m2 ->
            m1?.plus(m2) ?: m2
        } ?: SerializersModule { }
    }

    override fun createDefault() = Json {
        encodeDefaults = true
        isLenient = true
        allowSpecialFloatingPointValues = true
        allowStructuredMapKeys = true
        prettyPrint = true
        useArrayPolymorphism = false
        serializersModule = mergedModule
    }
}
