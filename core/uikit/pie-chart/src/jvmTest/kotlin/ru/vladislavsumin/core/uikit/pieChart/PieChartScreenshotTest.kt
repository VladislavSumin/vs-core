package ru.vladislavsumin.core.uikit.pieChart

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.uikit.screenshot.screenshotTest
import kotlin.test.Test

internal class PieChartScreenshotTest {
    @Test
    fun pieChart() {
        screenshotTest(
            goldenName = "pie_chart",
            size = DpSize(200.dp, 200.dp),
        ) {
            PieChart(
                pies = listOf(
                    Slice(fraction = 1f, color = Color.Red),
                    Slice(fraction = 2f, color = Color.Green),
                    Slice(fraction = 3f, color = Color.Blue),
                ),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
