package ru.vladislavsumin.core.uikit.screenshot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.dropbox.differ.SimpleImageComparator
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.roborazziSystemPropertyTaskType
import io.github.takahirom.roborazzi.captureRoboImage

/**
 * Тег корневой ноды, по которой снимается скриншот. Позволяет захватить именно [content] заданного размера,
 * а не весь виртуальный экран compose теста.
 */
private const val ROOT_TAG = "screenshot_root"

/**
 * Каталог с эталонными изображениями относительно директории модуля (рабочей директории gradle test таски).
 */
private const val GOLDEN_DIR = "src/jvmTest/screenshots"

/**
 * Порог доли изменившихся пикселей, при котором тест считается упавшим. Небольшое значение нужно для компенсации
 * различий сглаживания (anti-aliasing).
 */
private const val DEFAULT_CHANGE_THRESHOLD = 0.01F

/**
 * Максимальная нормализованная дистанция между пикселями, при которой они считаются одинаковыми.
 */
private const val DEFAULT_MAX_DISTANCE = 0.01F

/**
 * Сдвиг в пикселях при сравнении, помогает сгладить артефакты anti-aliasing по краям.
 */
private const val DEFAULT_SHIFT = 2

/**
 * Размер снимаемого контента по умолчанию.
 */
private val DEFAULT_SIZE = DpSize(200.dp, 200.dp)

/**
 * Опции сравнения скриншотов по умолчанию с небольшим допуском на различия сглаживания между рендерами.
 */
@OptIn(ExperimentalRoborazziApi::class)
public val defaultScreenshotRoborazziOptions: RoborazziOptions = RoborazziOptions(
    compareOptions = RoborazziOptions.CompareOptions(
        changeThreshold = DEFAULT_CHANGE_THRESHOLD,
        imageComparator = SimpleImageComparator(
            maxDistance = DEFAULT_MAX_DISTANCE,
            vShift = DEFAULT_SHIFT,
            hShift = DEFAULT_SHIFT,
        ),
    ),
)

/**
 * Запускает скриншот тест [content].
 *
 * Контент рендерится в виртуальном compose экране (desktop skiko рендер) и сравнивается попиксельно с эталоном
 * `src/jvmTest/screenshots/[goldenName].png`.
 *
 * Поведение управляется roborazzi таск ами / gradle свойствами:
 * - `recordRoborazziJvm` (или `-Proborazzi.test.record=true`) — записать/перезаписать эталон.
 * - `verifyRoborazziJvm` (или `-Proborazzi.test.verify=true`) — упасть при отличии от эталона.
 * - `compareRoborazziJvm` — сгенерировать diff изображение без падения теста.
 *
 * Если ни одна из roborazzi задач не активна (например обычный прогон `test allTests`), тест является полным no-op:
 * skiko не грузится и рендер не выполняется. Это позволяет запускать общие unit тесты в том числе на окружениях,
 * где нативный skiko недоступен (например CI на Linux).
 *
 * @param goldenName имя эталонного файла без расширения.
 * @param size размер снимаемого контента.
 * @param roborazziOptions опции захвата и сравнения.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
public fun screenshotTest(
    goldenName: String,
    size: DpSize = DEFAULT_SIZE,
    roborazziOptions: RoborazziOptions = defaultScreenshotRoborazziOptions,
    content: @Composable () -> Unit,
) {
    // Рендерим (и грузим нативный skiko) только когда активна одна из roborazzi задач record/compare/verify.
    if (!roborazziSystemPropertyTaskType().isEnabled()) return

    runDesktopComposeUiTest(2560, 1440) {
        setContent {
            Box(
                modifier = Modifier
                    .testTag(ROOT_TAG)
                    .size(size),
            ) {
                content()
            }
        }
        onNodeWithTag(ROOT_TAG).captureRoboImage(
            filePath = "$GOLDEN_DIR/$goldenName.png",
            roborazziOptions = roborazziOptions,
        )
    }
}
