package ru.vladislavsumin.core.navigation.serializer

import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.consumeRequired
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.repository.NavigationRepository
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.reflect.KClass
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class NavigationSerializer(repository: NavigationRepository) {

    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(IntentScreenParams::class) {
                repository.serializers.forEach { (clazz, serializer) ->
                    subclass(
                        subclass = clazz.key as KClass<IntentScreenParams<ScreenIntent>>,
                        serializer = serializer as KSerializer<IntentScreenParams<ScreenIntent>>,
                    )
                }
            }
        }
    }

    inline fun <reified T> encodeToSerializedContainer(data: T): SerializableContainer {
        val data = json.encodeToString(data)
        return SerializableContainer(data, String.serializer())
    }

    inline fun <reified T> decodeFromSerializedContainer(container: SerializableContainer): T {
        val encoded = container.consumeRequired(String.serializer())
        return json.decodeFromString(encoded)
    }

}
