package ru.vladislavsumin.core.navigation.screen

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.GenericComponentContext
import ru.vladislavsumin.core.decompose.compose.ComposeComponent

public typealias Screen = GenericComposeScreen<ComponentContext>

public abstract class GenericComposeScreen<Ctx : GenericComponentContext<Ctx>>(
    context: Ctx,
) : GenericScreen<Ctx, GenericComposeScreen<Ctx>>(context), ComposeComponent
