package ru.vladislavsumin.core.navigation

import org.kodein.di.DI
import org.kodein.di.bindSet
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.ui.debug.uml.NavigationGraphUmlDiagramComponentFactory

public fun Modules.coreNavigation(): DI.Module = DI.Module("core-navigation") {
    // Декларируем множество, в которое будут собраны все регистраторы навигации в приложении.
    bindSet<NavigationRegistrar>()

    // Я не нашел, как нормально разорвать цикл зависимостей в kodein, поэтому пришлось добавить такой костыль.
    var navigation: Navigation? = null

    bindSingleton { Navigation(registrars = i()).also { navigation = it } }

    bindSingleton {
        NavigationGraphUmlDiagramComponentFactory { navigation!! }
    }
}
