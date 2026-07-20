package ru.vladislavsumin.core.navigation.viewModel

import ru.vladislavsumin.core.navigation.InternalNavigationApi

/**
 * Внутренняя переменная маркер, означающая, что мы находимся внутри функции конструирования
 * [ru.vladislavsumin.core.navigation.screen.Screen.viewModel]. Это используется только для проверки, что модель
 * создается правильно
 */
@Suppress("PropertyName")
@InternalNavigationApi
public var IsNavigationViewModelConstructing: Boolean = false
