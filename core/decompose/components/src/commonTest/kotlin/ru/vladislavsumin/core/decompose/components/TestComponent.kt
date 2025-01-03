package ru.vladislavsumin.core.decompose.components

import com.arkivanov.decompose.ComponentContext

class TestComponent(context: ComponentContext) : Component(context) {
    val testViewModel = viewModel { TestViewModel() }
}
