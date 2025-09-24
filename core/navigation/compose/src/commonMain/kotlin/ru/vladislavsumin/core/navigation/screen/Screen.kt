package ru.vladislavsumin.core.navigation.screen

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.GenericComponentContext

public typealias Screen = GenericComposeScreen<ComponentContext>

public abstract class GenericComposeScreen<Ctx : GenericComponentContext<Ctx>>(
    context: Ctx,
) : GenericScreen<Ctx, ComposeRender>(context), ComposeRender {
    final override val render: ComposeRender = this
}
