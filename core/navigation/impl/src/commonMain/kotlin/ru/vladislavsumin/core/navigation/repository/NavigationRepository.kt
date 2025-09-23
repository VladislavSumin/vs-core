package ru.vladislavsumin.core.navigation.repository

import com.arkivanov.decompose.GenericComponentContext
import kotlinx.serialization.KSerializer
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistry
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenKey
import ru.vladislavsumin.core.navigation.tree.NavigationTree
import kotlin.reflect.KClass

/**
 * Репозиторий навигации, используется для построения [NavigationTree].
 */
internal interface NavigationRepository<Ctx : GenericComponentContext<Ctx>> {
    /**
     * Список всех зарегистрированных экранов.
     */
    val screens: Map<ScreenKey, ScreenRegistration<Ctx>>

    /**
     * Множество [KSerializer] для сериализации [ScreenParams].
     */
    val serializers: Map<ScreenKey, KSerializer<out IntentScreenParams<*>>>
}

/**
 * Регистрирует все компоненты навигации.
 *
 * @param registrars множество регистраторов экранов.
 */
internal class NavigationRepositoryImpl<Ctx : GenericComponentContext<Ctx>>(
    registrars: Set<GenericNavigationRegistrar<Ctx>>,
) : NavigationRepository<Ctx> {
    override val screens = mutableMapOf<ScreenKey, ScreenRegistration<Ctx>>()
    override val serializers = mutableMapOf<ScreenKey, KSerializer<out IntentScreenParams<*>>>()

    /**
     * Состояние финализации [NavigationRegistry]. После создания [NavigationRepositoryImpl] добавлять новые элементы
     * нельзя.
     */
    private var isFinalized = false

    private val registry = NavigationRegistryImpl()

    init {
        registrars.forEach { registry.register(it) }
        isFinalized = true
    }

    private inner class NavigationRegistryImpl : NavigationRegistry<Ctx>() {
        fun register(registrar: GenericNavigationRegistrar<Ctx>) {
            with(registrar) { register() }
        }

        override fun registerScreen(
            key: ScreenKey,
            factory: ScreenFactory<Ctx, *, *, *>?,
            paramsSerializer: KSerializer<out IntentScreenParams<*>>,
            defaultParams: IntentScreenParams<*>?,
            description: String?,
            navigationHosts: HostRegistry.() -> Unit,
        ) {
            if (isFinalized) {
                throw ScreenRegistrationAfterFinalizeException(key)
            }

            serializers[key] = paramsSerializer

            val hostRegistry = HostRegistryImpl(key)
            navigationHosts(hostRegistry)
            val navigationHosts = hostRegistry.build()

            val screenRegistration = ScreenRegistration(
                factory = factory,
                defaultParams = defaultParams,
                navigationHosts = navigationHosts,
                description = description,
            )
            val oldRegistration = screens.put(key, screenRegistration)
            if (oldRegistration != null) {
                throw DoubleScreenRegistrationException(key)
            }
        }
    }

    private class HostRegistryImpl(private val parentScreen: ScreenKey) : NavigationRegistry.HostRegistry {
        private val hosts = mutableMapOf<NavigationHost, Set<ScreenKey>>()
        override fun NavigationHost.opens(screens: Set<KClass<out IntentScreenParams<*>>>) {
            val oldRegistration = hosts.put(this, screens.map { ScreenKey(it) }.toSet())
            if (oldRegistration != null) {
                throw DoubleHostRegistrationException(parentScreen, this)
            }
        }

        fun build(): Map<NavigationHost, Set<ScreenKey>> {
            // Проверяем двойную регистрацию экранов в разных хостах общего родителя.
            val alreadyRegisteredScreens = mutableSetOf<ScreenKey>()
            hosts.values.forEach { screens ->
                screens.forEach { screen ->
                    if (!alreadyRegisteredScreens.add(screen)) {
                        throw MultipleScreenRegistrationInSameParentException(parentScreen, screen)
                    }
                }
            }

            return hosts
        }
    }
}
