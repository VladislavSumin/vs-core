package ru.vladislavsumin.core.navigation.registration

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenKey
import kotlin.reflect.KClass

/**
 * Позволяет регистрировать компоненты навигации. Использовать напрямую этот интерфейс нельзя, так как его состояние
 * финализируется в процессе инициализации приложения. Для доступа к [NavigationRegistry]
 * воспользуйтесь [NavigationRegistrar].
 *
 * Абстрактный класс вместо интерфейса для возможности использовать internal && inline для создания удобного апи.
 */
public abstract class NavigationRegistry {
    /**
     * Регистрирует экран.
     *
     * @param P тип параметров экрана.
     * @param S тип экрана.
     * @param factory фабрика компонента экрана, может быть явно задана как null, если используются customFactories.
     * @param defaultParams параметры экрана по умолчанию.
     * @param navigationHosts хосты навигации на этом экране, а также экраны, которые они могут открывать.
     * @param description опциональное описание экрана, используется только для дебага, при отображении графа навигации
     */
    public inline fun <reified P : IntentScreenParams<I>, I : ScreenIntent, S : Screen> registerScreen(
        factory: ScreenFactory<P, I, S>?,
        defaultParams: P? = null,
        description: String? = null,
        noinline navigationHosts: HostRegistry.() -> Unit = {},
    ): Unit = registerScreen(
        ScreenKey(P::class),
        factory,
        Json.serializersModule.serializer<P>(),
        defaultParams,
        description,
        navigationHosts,
    )

    @PublishedApi
    internal abstract fun <P : IntentScreenParams<I>, I : ScreenIntent, S : Screen> registerScreen(
        key: ScreenKey,
        factory: ScreenFactory<P, I, S>?,
        paramsSerializer: KSerializer<P>,
        defaultParams: P?,
        description: String?,
        navigationHosts: HostRegistry.() -> Unit,
    )

    public interface HostRegistry {
        public infix fun NavigationHost.opens(screens: Set<KClass<out IntentScreenParams<*>>>)
    }
}
