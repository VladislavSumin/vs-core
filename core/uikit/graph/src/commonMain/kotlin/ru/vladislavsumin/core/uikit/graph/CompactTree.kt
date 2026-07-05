package ru.vladislavsumin.core.uikit.graph

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.collections.tree.TreeNode
import kotlin.math.max

/**
 * Небольшой отступ, на который линия связи входит в левую часть родительской ноды при вертикальном
 * расположении дочерних листьев.
 */
private val CONNECTOR_INSET = 24.dp

/**
 * Длина горизонтального "отростка" от вертикальной линии-шины до левого края дочерней ноды при
 * вертикальном расположении дочерних листьев.
 */
private val CHILD_INDENT = 16.dp

/**
 * Рисует древовидную структуру. Сверху вниз, соединяет корневую node со всеми дочерними и рисует соединительные линии.
 *
 * В отличие от [Tree], если у ноды **два и более** потомков и **все** они являются листьями (не имеют собственных
 * потомков), то такие потомки располагаются вертикально столбцом, а не горизонтально. Связывающая их с родителем
 * линия выходит из левого края каждого потомка, идёт к общей вертикальной линии-шине и поднимается в левую часть
 * родителя (с небольшим отступом от левого края). Это позволяет делать граф визуально плотнее по ширине.
 *
 * @param rootNode вершина дерева.
 * @param verticalSpace вертикальное расстояние между родительской нодой и дочерними.
 * @param horizontalSpace горизонтальное расстояние между дочерними нодами (при вертикальном расположении используется
 * как вертикальный зазор между дочерними нодами).
 * @param lineColor цвет линии, соединяющей ноды.
 * @param lineWidth толщина линии, соединяющей ноды.
 * @param content контент одной ноды дерева.
 */
@Composable
public fun <T, N : TreeNode<T, N>> CompactTree(
    rootNode: TreeNode<T, N>,
    modifier: Modifier = Modifier,
    verticalSpace: Dp = 24.dp,
    horizontalSpace: Dp = 16.dp,
    lineColor: Color,
    lineWidth: Dp = 1.dp,
    content: @Composable (T) -> Unit,
) {
    val drawState = remember { mutableStateOf<LineSpec?>(null) }

    Layout(
        content = {
            CompactLines(drawState, lineColor, lineWidth)
            content(rootNode.value)
            rootNode.children.forEach {
                CompactTree(
                    rootNode = it,
                    verticalSpace = verticalSpace,
                    horizontalSpace = horizontalSpace,
                    lineColor = lineColor,
                    lineWidth = lineWidth,
                    content = content,
                )
            }
        },
        modifier = modifier,
        measurePolicy = { children, _ ->
            // Так как контент должен помещаться в scale контейнер, то мы никак не ограничиваем
            // размеры дочерних элементов
            val infiniteWrapContentConstraints = Constraints()

            // Canvas для отрисовки вспомогательных элементов
            val canvasMeasurable = children[0]

            // Главный элемент, представляет собой переданную node. Всегда строго один.
            val root = children[1].measure(infiniteWrapContentConstraints)

            // Дочерние элементы.
            val childNodes = children.drop(2).map { it.measure(infiniteWrapContentConstraints) }

            // Если у ноды 2+ потомков и все они листья — располагаем их вертикально, иначе классической строчкой.
            val useVerticalLayout = childNodes.size >= 2 && rootNode.children.all { it.children.isEmpty() }

            if (useVerticalLayout) {
                measureVertical(canvasMeasurable, root, childNodes, verticalSpace, horizontalSpace, drawState)
            } else {
                measureHorizontal(canvasMeasurable, root, childNodes, verticalSpace, horizontalSpace, drawState)
            }
        },
    )
}

/**
 * Классическое горизонтальное расположение дочерних нод в строчку под родительской.
 */
private fun MeasureScope.measureHorizontal(
    canvasMeasurable: Measurable,
    root: Placeable,
    childNodes: List<Placeable>,
    verticalSpace: Dp,
    horizontalSpace: Dp,
    drawState: MutableState<LineSpec?>,
): MeasureResult {
    val horizontalSpacePx = horizontalSpace.toPx().toInt()
    val verticalSpacePx = if (childNodes.isEmpty()) 0 else verticalSpace.toPx().toInt()

    val childrenTotalSpacePx = max(horizontalSpacePx * (childNodes.size - 1), 0)
    val childTotalWidth = childNodes.sumOf { it.width } + childrenTotalSpacePx
    val width = max(root.width, childTotalWidth)
    val height = root.height + (childNodes.maxOfOrNull { it.height } ?: 0) + verticalSpacePx

    val canvas = canvasMeasurable.measure(Constraints.fixed(width, height))

    return layout(width, height) {
        // Главный элемент размещаем сверху по центру.
        root.place(width / 2 - root.width / 2, 0)

        // Canvas занимает всю область рисования.
        canvas.place(0, 0)

        // Бывает что дочерние элементы уже родительского, тогда они должны быть выровнены по центру
        // относительно родительского. Учитываем этот момент при вычислении начального отступа по ширине.
        var currentWidth = (width - childTotalWidth) / 2
        // Далее дочерние элементы расставляем в строчку под родительским с учетом всех отступов.
        val childCentersX = childNodes.map {
            it.place(currentWidth, root.height + verticalSpacePx)
            val centerX = currentWidth + it.width / 2
            currentWidth += it.width + horizontalSpacePx
            centerX
        }
        if (childNodes.isNotEmpty()) {
            drawState.value = LineSpec.Horizontal(
                endRootY = root.height,
                centerRootX = width / 2,
                startChildY = root.height + verticalSpacePx,
                childCentersX = childCentersX,
            )
        }
    }
}

/**
 * Вертикальное расположение дочерних нод-листьев столбцом, отступив вправо от линии-шины.
 */
private fun MeasureScope.measureVertical(
    canvasMeasurable: Measurable,
    root: Placeable,
    childNodes: List<Placeable>,
    verticalSpace: Dp,
    horizontalSpace: Dp,
    drawState: MutableState<LineSpec?>,
): MeasureResult {
    val verticalSpacePx = verticalSpace.toPx().toInt()
    val siblingGapPx = horizontalSpace.toPx().toInt()
    val connectorInsetPx = CONNECTOR_INSET.toPx().toInt()
    val childIndentPx = CHILD_INDENT.toPx().toInt()

    val maxChildWidth = childNodes.maxOf { it.width }
    // Правый край дочерних нод относительно левого края родителя.
    val childRightRel = connectorInsetPx + childIndentPx + maxChildWidth
    // Держим родителя по центру бокса: если дети шире — компенсируем отступом слева.
    val rightOverhang = max(0, childRightRel - root.width)
    val rootLeft = rightOverhang
    val width = root.width + 2 * rightOverhang
    val busX = rootLeft + connectorInsetPx
    val childLeft = rootLeft + connectorInsetPx + childIndentPx

    val childrenHeight = childNodes.sumOf { it.height } + siblingGapPx * (childNodes.size - 1)
    val height = root.height + verticalSpacePx + childrenHeight

    val canvas = canvasMeasurable.measure(Constraints.fixed(width, height))

    return layout(width, height) {
        root.place(rootLeft, 0)
        canvas.place(0, 0)

        var currentY = root.height + verticalSpacePx
        val childCentersY = childNodes.map {
            it.place(childLeft, currentY)
            val centerY = currentY + it.height / 2
            currentY += it.height + siblingGapPx
            centerY
        }

        drawState.value = LineSpec.Vertical(
            busX = busX,
            topY = root.height,
            childStubX = childLeft,
            childCentersY = childCentersY,
        )
    }
}

/**
 * Вспомогательная функция рисует линии соединяющие ноды на основе [spec].
 */
@Composable
private fun CompactLines(spec: State<LineSpec?>, lineColor: Color, lineWidth: Dp) {
    Canvas(Modifier) {
        val lineWidthPx = lineWidth.toPx()
        when (val state = spec.value) {
            null -> Unit
            is LineSpec.Horizontal -> drawHorizontalLines(state, lineColor, lineWidthPx)
            is LineSpec.Vertical -> drawVerticalLines(state, lineColor, lineWidthPx)
        }
    }
}

private fun DrawScope.drawHorizontalLines(state: LineSpec.Horizontal, lineColor: Color, lineWidthPx: Float) =
    with(state) {
        val midY = endRootY.toFloat() + (startChildY - endRootY) / 2

        // Линия от главного к центру.
        drawLine(
            color = lineColor,
            start = Offset(centerRootX.toFloat(), endRootY.toFloat()),
            end = Offset(centerRootX.toFloat(), midY),
            strokeWidth = lineWidthPx,
            cap = StrokeCap.Round,
        )

        // Горизонтальная линия.
        drawLine(
            color = lineColor,
            start = Offset(childCentersX.first().toFloat(), midY),
            end = Offset(childCentersX.last().toFloat(), midY),
            strokeWidth = lineWidthPx,
            cap = StrokeCap.Round,
        )

        // Вертикальные линии к дочерним элементам.
        childCentersX.forEach { childCenterX ->
            drawLine(
                color = lineColor,
                start = Offset(childCenterX.toFloat(), midY),
                end = Offset(childCenterX.toFloat(), startChildY.toFloat()),
                strokeWidth = lineWidthPx,
                cap = StrokeCap.Round,
            )
        }
    }

private fun DrawScope.drawVerticalLines(state: LineSpec.Vertical, lineColor: Color, lineWidthPx: Float) = with(state) {
    // Вертикальная линия-шина от левой части родителя вниз до последнего потомка.
    drawLine(
        color = lineColor,
        start = Offset(busX.toFloat(), topY.toFloat()),
        end = Offset(busX.toFloat(), childCentersY.last().toFloat()),
        strokeWidth = lineWidthPx,
        cap = StrokeCap.Round,
    )

    // Горизонтальные отростки от шины к левому краю каждого потомка.
    childCentersY.forEach { childCenterY ->
        drawLine(
            color = lineColor,
            start = Offset(busX.toFloat(), childCenterY.toFloat()),
            end = Offset(childStubX.toFloat(), childCenterY.toFloat()),
            strokeWidth = lineWidthPx,
            cap = StrokeCap.Round,
        )
    }
}

/**
 * Вспомогательный класс для передачи координат точек необходимых для построения линий из фазы measure/layout в
 * фазу draw.
 */
private sealed interface LineSpec {
    data class Horizontal(
        val endRootY: Int,
        val centerRootX: Int,
        val startChildY: Int,
        val childCentersX: List<Int>,
    ) : LineSpec

    data class Vertical(val busX: Int, val topY: Int, val childStubX: Int, val childCentersY: List<Int>) : LineSpec
}
