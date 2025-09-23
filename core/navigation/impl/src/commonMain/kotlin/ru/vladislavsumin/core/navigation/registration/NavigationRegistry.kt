package ru.vladislavsumin.core.navigation.registration

import com.arkivanov.decompose.GenericComponentContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenKey
import kotlin.reflect.KClass

/**
 * Позволяет регистрировать компоненты навигации. Использовать напрямую этот интерфейс нельзя, так как его состояние
 * финализируется в процессе инициализации приложения. Для доступа к [NavigationRegistry]
 * воспользуйтесь [GenericNavigationRegistrar].
 *
 * Абстрактный класс вместо интерфейса для возможности использовать internal && inline для создания удобного апи.
 */
public abstract class NavigationRegistry<Ctx : GenericComponentContext<Ctx>> {
    /**
     * Регистрирует экран.
     *
     * @param P тип параметров экрана.
     * @param S тип экрана.
     * @param factory фабрика компонента экрана.
     * @param defaultParams параметры экрана по умолчанию.
     * @param navigationHosts хосты навигации на этом экране, а также экраны, которые они могут открывать.
     * @param description опциональное описание экрана, используется только для дебага, при отображении графа навигации
     */
    public inline fun <reified P : IntentScreenParams<I>, I : ScreenIntent, S : GenericScreen<Ctx>> registerScreen(
        factory: ScreenFactory<Ctx, P, I, S>,
        defaultParams: P? = null,
        description: String? = null,
        noinline navigationHosts: HostRegistry.() -> Unit = {},
    ): Unit = registerScreen(
        key = ScreenKey(P::class),
        factory = factory,
        paramsSerializer = Json.serializersModule.serializer<P>(),
        defaultParams = defaultParams,
        description = description,
        navigationHosts = navigationHosts,
    )

    /**
     * Регистрирует экран с custom factory. Фабрика такого экрана должна быть задана явно в родительском экране через
     * [GenericScreen.registerCustomFactory].
     */
    public inline fun <reified P : IntentScreenParams<*>> registerScreen(
        defaultParams: P? = null,
        description: String? = null,
        noinline navigationHosts: HostRegistry.() -> Unit = {},
    ): Unit = registerScreen(
        key = ScreenKey(P::class),
        factory = null,
        paramsSerializer = Json.serializersModule.serializer<P>(),
        defaultParams = defaultParams,
        description = description,
        navigationHosts = navigationHosts,
    )

    /**
     * Приватный unsafe метод регистрации. Обратите внимание, при вызове необходимо обеспечивать совместимость типов
     * всех параметров.
     */
    @PublishedApi
    internal abstract fun registerScreen(
        key: ScreenKey,
        factory: ScreenFactory<Ctx, *, *, *>?,
        paramsSerializer: KSerializer<out IntentScreenParams<*>>,
        defaultParams: IntentScreenParams<*>?,
        description: String?,
        navigationHosts: HostRegistry.() -> Unit,
    )

    public interface HostRegistry {
        public infix fun NavigationHost.opens(screens: Set<KClass<out IntentScreenParams<*>>>)
    }
}
