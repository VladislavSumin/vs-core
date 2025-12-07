package ru.vladislavsumin.core.serialization.yaml

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.modules.SerializersModule
import ru.vladislavsumin.core.serialization.core.SerializersModulesRepository

internal interface YamlFactory {
    /**
     * Создает default [ProtoBuf] инстанс. Добавляет в него [SerializersModule] объявленные в DI графе.
     */
    fun createDefault(): Yaml
}

internal class YamlFactoryImpl(
    private val serializersModulesRepository: SerializersModulesRepository,
) : YamlFactory {

    override fun createDefault() = Yaml(
        serializersModule = serializersModulesRepository.serializerModule,
    )
}
