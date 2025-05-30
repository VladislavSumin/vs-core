package ru.vladislavsumin.core.navigation.viewModel

/**
 * Внутренняя переменная маркер, означающая, что мы находимся внутри функции конструирования
 * [ru.vladislavsumin.core.navigation.screen.Screen.viewModel]. Это используется только для проверки, что модель
 * создается правильно
 */
internal var IsNavigationViewModelConstructing: Boolean = false
