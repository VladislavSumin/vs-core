package ru.vladislavsumin.core.serialization.protobuf

import kotlinx.serialization.protobuf.ProtoBuf
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.core.serialization.core.coreSerializationCore

public fun Modules.coreSerializationProtobuf(): DI.Module = DI.Module("core-serialization-protobuf") {
    importOnce(Modules.coreSerializationCore())

    bindSingleton<ProtobufFactory> { ProtobufFactoryImpl(i()) }

    /**
     * Default [ProtoBuf] инстанс кладется в граф для возможности получить к нему доступ из любой точки приложения.
     */
    bindSingleton<ProtoBuf> { i<ProtobufFactory>().createDefault() }
}
