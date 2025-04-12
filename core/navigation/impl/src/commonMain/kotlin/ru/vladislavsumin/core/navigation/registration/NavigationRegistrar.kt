package ru.vladislavsumin.core.navigation.registration

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
public fun interface NavigationRegistrar {
    /**
     * Регистрирует фабрики, хосты навигации и экраны в хостах.
     */
    public fun NavigationRegistry.register()
}
