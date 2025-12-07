package ru.vladislavsumin.core.serialization.yaml

import com.charleskorn.kaml.Yaml
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.core.serialization.core.coreSerializationCore

public fun Modules.coreSerializationYaml(): DI.Module = DI.Module("core-serialization-yaml") {
    importOnce(Modules.coreSerializationCore())

    bindSingleton<YamlFactory> { YamlFactoryImpl(i()) }

    /**
     * Default [Yaml] инстанс кладется в граф для возможности получить к нему доступ из любой точки приложения.
     */
    bindSingleton<Yaml> { i<YamlFactory>().createDefault() }
}
