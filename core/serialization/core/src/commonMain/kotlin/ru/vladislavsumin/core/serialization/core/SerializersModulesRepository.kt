package ru.vladislavsumin.core.serialization.core

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus

@InternalSerializationApi
public interface SerializersModulesRepository {
    /**
     * Возвращает общий [SerializersModule] для всех модулей зарегистрированных через di.
     */
    public val serializerModule: SerializersModule
}

internal class SerializersModulesRepositoryImpl(
    serializersModulesSet: Set<SerializersModule>,
) : SerializersModulesRepository {
    override val serializerModule: SerializersModule by lazy {
        /**
         * Конкатенирует все [serializersModulesSet] в один [SerializersModule].
         */
        serializersModulesSet.fold(null as SerializersModule?) { m1, m2 ->
            m1?.plus(m2) ?: m2
        } ?: SerializersModule { }
    }
}
