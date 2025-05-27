package ru.vladislavsumin.core.navigation.repository

import kotlinx.serialization.KSerializer
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistry
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenKey
import ru.vladislavsumin.core.navigation.tree.NavigationTree
import kotlin.reflect.KClass

/**
 * Репозиторий навигации, используется для построения [NavigationTree].
 */
internal interface NavigationRepository {
    /**
     * Список всех зарегистрированных экранов.
     */
    val screens: Map<ScreenKey, ScreenRegistration<*, *>>

    /**
     * Множество [KSerializer] для сериализации [ScreenParams].
     */
    val serializers: Map<ScreenKey, KSerializer<out ScreenParams>>
}

/**
 * Регистрирует все компоненты навигации.
 *
 * @param registrars множество регистраторов экранов.
 */
internal class NavigationRepositoryImpl(
    registrars: Set<NavigationRegistrar>,
) : NavigationRepository {
    override val screens = mutableMapOf<ScreenKey, ScreenRegistration<*, *>>()
    override val serializers = mutableMapOf<ScreenKey, KSerializer<out ScreenParams>>()

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

    private inner class NavigationRegistryImpl : NavigationRegistry() {
        fun register(registrar: NavigationRegistrar) {
            with(registrar) { register() }
        }

        override fun <P : ScreenParams, S : Screen> registerScreen(
            key: ScreenKey,
            factory: ScreenFactory<P, S>?,
            paramsSerializer: KSerializer<P>,
            defaultParams: P?,
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
        override fun NavigationHost.opens(screens: Set<KClass<out ScreenParams>>) {
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
