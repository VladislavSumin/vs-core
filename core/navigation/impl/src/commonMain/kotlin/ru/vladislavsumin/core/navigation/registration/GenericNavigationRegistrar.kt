package ru.vladislavsumin.core.navigation.registration

import com.arkivanov.decompose.GenericComponentContext
import ru.vladislavsumin.core.navigation.screen.Render

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
public fun interface GenericNavigationRegistrar<Ctx : GenericComponentContext<Ctx>, R : Render> {
    /**
     * Регистрирует фабрики, хосты навигации и экраны в хостах.
     */
    public fun NavigationRegistry<Ctx, R>.register()
}
