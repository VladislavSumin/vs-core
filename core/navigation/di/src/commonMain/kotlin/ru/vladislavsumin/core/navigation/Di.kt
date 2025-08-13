package ru.vladislavsumin.core.navigation

import com.arkivanov.decompose.GenericComponentContext
import org.kodein.di.DI
import org.kodein.di.bindSet
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.ui.debug.uml.NavigationGraphUmlDiagramComponentFactory

public inline fun <reified Ctx : GenericComponentContext<Ctx>> Modules.coreNavigation(): DI.Module =
    DI.Module("core-navigation") {
        // Декларируем множество, в которое будут собраны все регистраторы навигации в приложении.
        bindSet<GenericNavigationRegistrar<Ctx>>()

        // Я не нашел, как нормально разорвать цикл зависимостей в kodein, поэтому пришлось добавить такой костыль.
        var navigation: GenericNavigation<Ctx>? = null

        bindSingleton { GenericNavigation<Ctx>(registrars = i()).also { navigation = it } }

        bindSingleton {
            NavigationGraphUmlDiagramComponentFactory<Ctx> { navigation!! }
        }
    }
