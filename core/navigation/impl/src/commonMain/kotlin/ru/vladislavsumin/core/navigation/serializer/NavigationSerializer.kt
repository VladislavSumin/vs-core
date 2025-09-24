package ru.vladislavsumin.core.navigation.serializer

import com.arkivanov.essenty.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.essenty.statekeeper.polymorphicSerializer
import kotlinx.serialization.ExperimentalSerializationApi
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

internal class NavigationSerializer(
    repository: NavigationRepository<*, *>,
) {
    /**
     * Сериализатор для всех зарегистрированных [ScreenParams], используется внутри decompose для сохранения и
     * восстановления состояния приложения.
     */
    @OptIn(ExperimentalSerializationApi::class, ExperimentalStateKeeperApi::class)
    val serializer: KSerializer<IntentScreenParams<*>> = polymorphicSerializer(
        IntentScreenParams::class,
        SerializersModule {
            polymorphic(IntentScreenParams::class) {
                repository.serializers.forEach { (clazz, serializer) ->
                    subclass(
                        subclass = clazz.key as KClass<IntentScreenParams<ScreenIntent>>,
                        serializer = serializer as KSerializer<IntentScreenParams<ScreenIntent>>,
                    )
                }
            }
        },
    ) as KSerializer<IntentScreenParams<*>> // TODO прочекать это
}
