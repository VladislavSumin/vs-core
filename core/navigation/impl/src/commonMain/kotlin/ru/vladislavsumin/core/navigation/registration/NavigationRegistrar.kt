package ru.vladislavsumin.core.navigation.registration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.GenericComponentContext

public typealias NavigationRegistrar = GenericNavigationRegistrar<ComponentContext>

/**
 * Если вы используете kodein, то этот интерфейс необходимо реализовать в вашем модуле и вернуть его в графе навигации
 * через [bindNavigation]:
 * ```kotlin
 * bindNavigation {
 *     NavigationRegistrarImpl()
 * }
 * ```
 * После чего можно зарегистрировать компоненты навигации получив [NavigationRegistry] в методе [register].
 *
 * Если вы не используете kodein используйте конструктор [ru.vladislavsumin.core.navigation.Navigation]
 */
public fun interface GenericNavigationRegistrar<Ctx : GenericComponentContext<Ctx>> {
    /**
     * Регистрирует фабрики, хосты навигации и экраны в хостах.
     */
    public fun NavigationRegistry<Ctx>.register()
}
