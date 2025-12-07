package ru.vladislavsumin.core.serialization.protobuf

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.protobuf.ProtoBuf
import ru.vladislavsumin.core.serialization.core.SerializersModulesRepository

internal interface ProtobufFactory {
    /**
     * Создает default [ProtoBuf] инстанс. Добавляет в него [SerializersModule] объявленные в DI графе.
     */
    fun createDefault(): ProtoBuf
}

internal class ProtobufFactoryImpl(
    private val serializersModulesRepository: SerializersModulesRepository,
) : ProtobufFactory {

    override fun createDefault() = ProtoBuf {
        serializersModule = serializersModulesRepository.serializerModule
    }
}
