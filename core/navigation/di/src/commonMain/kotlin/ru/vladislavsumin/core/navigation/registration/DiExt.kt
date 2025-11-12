package ru.vladislavsumin.core.navigation.registration

import com.arkivanov.decompose.GenericComponentContext
import org.kodein.di.DI
import org.kodein.di.DirectDIAware
import org.kodein.di.inBindSet
import org.kodein.di.provider
import ru.vladislavsumin.core.navigation.screen.GenericScreen

/**
 * Синтаксический сахар для регистрации навигации.
 * @see GenericNavigationRegistrar
 */
public inline fun <
    Ctx : GenericComponentContext<Ctx>,
    BS : GenericScreen<Ctx, BS>,
    reified T : GenericNavigationRegistrar<Ctx, BS>,
    > DI.Builder.bindGenericNavigation(
    crossinline block: DirectDIAware.() -> T,
) {
    inBindSet<GenericNavigationRegistrar<Ctx, BS>> {
        add { provider { block() } }
    }
}
