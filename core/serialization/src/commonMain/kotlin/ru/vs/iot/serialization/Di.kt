package ru.vs.iot.serialization

import kotlinx.serialization.modules.SerializersModule
import org.kodein.di.DI
import org.kodein.di.bindSet
import org.kodein.di.bindSingleton
import ru.vs.core.di.Modules
import ru.vs.core.di.i

fun Modules.coreSerialization() = DI.Module("core-serialization") {
    bindSet<SerializersModule>()

    bindSingleton<JsonFactory> { JsonFactoryImpl(i()) }
    bindSingleton { i<JsonFactory>().createDefault() }
}
