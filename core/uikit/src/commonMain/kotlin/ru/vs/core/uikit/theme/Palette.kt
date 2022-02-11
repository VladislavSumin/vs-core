package ru.vs.core.uikit.theme

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

@Suppress("MagicNumber")
internal val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200,

    background = Color(0xFF2B2B2B),
    surface = Color(0xFF3B3F41),

    onPrimary = Color(0xFFBBBBBB),
    onSurface = Color(0xFFBBBBBB)
)

internal val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal200,

    background = Grey100,
)
