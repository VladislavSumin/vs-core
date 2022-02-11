package ru.vs.core.uikit.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun UKWaiting() {
    Box(Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            Modifier.align(Alignment.Center)
        )
    }
}

// @Suppress("UnusedPrivateMember")
// @Preview(showBackground = true)
// @Composable
// private fun UKWaitingPreview() {
//    MainTheme {
//        UKWaiting()
//    }
// }
