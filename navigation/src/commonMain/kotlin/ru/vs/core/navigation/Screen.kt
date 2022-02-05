package ru.vs.core.navigation

import androidx.compose.runtime.Composable
import com.arkivanov.essenty.parcelable.Parcelable

// TODO добавить документацию
interface Screen : Parcelable {
    @Composable
    fun ScreenView()
}
