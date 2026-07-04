package ru.vladislavsumin.core.navigation.ui.debug.uml

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.vladislavsumin.core.collections.tree.TreeNodeImpl
import ru.vladislavsumin.core.navigation.NavigationHost
import kotlin.math.max

private typealias UmlTreeNode = TreeNodeImpl<out NavigationGraphUmlNode>

/** Внутренние горизонтальные отступы карточки ноды. */
private val CARD_PADDING_H = 16.dp

/** Внутренние вертикальные отступы карточки ноды. */
private val CARD_PADDING_V = 8.dp

/** Отступ между заголовком ноды и строкой хостов навигации внутри карточки. */
private val HEADER_TO_HOSTS_GAP = 8.dp

/** Горизонтальное расстояние между слотами хостов. */
private val HOST_SPACING = 24.dp

/** Вертикальное расстояние между карточкой родителя и дочерними нодами. */
private val VERTICAL_SPACE = 24.dp

/** Горизонтальное расстояние между дочерними нодами (и вертикальное при компактной укладке листьев). */
private val HORIZONTAL_SPACE = 16.dp

/** Отступ, на который линия связи входит в левую часть хоста при вертикальной укладке листьев. */
private val CONNECTOR_INSET = 24.dp

/** Длина горизонтального отростка от линии-шины до левого края листа при вертикальной укладке. */
private val CHILD_INDENT = 16.dp

/** Радиус скругления карточки ноды. */
private val CARD_CORNER = 12.dp

/** Толщина рамки карточки ноды, входящей в основной граф. */
private val BORDER_WIDTH = 1.dp

/** Толщина пунктирной рамки карточки внешней ноды. */
private val DASH_BORDER_WIDTH = 2.dp

private const val DASH_ON_PX = 16f
private const val DASH_OFF_PX = 8f

/**
 * Отрисовывает навигационный граф с группировкой дочерних экранов по [NavigationHost].
 *
 * Каждая нода рисуется как карточка, внутри которой находится заголовок экрана и карточки его хостов навигации.
 * Дочерние экраны группируются по хосту, в котором они объявлены ([InternalNavigationGraphUmlNode.hostInParent]),
 * и располагаются под соответствующим хостом. Соединительные линии идут от конкретной карточки хоста к его дочерним
 * экранам. Если все дети одного хоста — листья, они укладываются компактно вертикально (как в [CompactTree]),
 * иначе горизонтально в строку.
 */
@Composable
internal fun NavigationGraphNode(
    node: UmlTreeNode,
    lineColor: Color,
    modifier: Modifier = Modifier,
    lineWidth: Dp = 1.dp,
) {
    val info = node.value
    val hosts = (info as? InternalNavigationGraphUmlNode)?.navigationHosts?.toList().orEmpty()
    val children = node.children.toList()
    val groups = buildGroups(hosts, children)
    val drawState = remember { mutableStateOf<NodeLines?>(null) }

    Layout(
        content = {
            NavGraphCanvas(drawState, lineColor, lineWidth)
            NavigationNodeHeader(info)
            hosts.forEach { NavigationHostCard(it) }
            groups.forEach { group ->
                group.nodes.forEach {
                    NavigationGraphNode(
                        it,
                        lineColor,
                        lineWidth = lineWidth,
                    )
                }
            }
        },
        modifier = modifier,
        measurePolicy = { measurables, _ ->
            val infinite = Constraints()
            val header = measurables[1].measure(infinite)
            val hostPlaceables = hosts.indices.map { measurables[2 + it].measure(infinite) }
            val childPlaceables = measurables.drop(2 + hosts.size).map { it.measure(infinite) }
            val metas = groups.map { it.toMeta() }

            val layout = computeNodeLayout(header, hostPlaceables, childPlaceables, metas, !info.isPartOfMainGraph)
            val canvas = measurables[0].measure(Constraints.fixed(layout.width, layout.height))
            drawState.value = layout.lines

            layout(layout.width, layout.height) {
                canvas.place(0, 0)
                header.place(layout.headerPosition)
                hostPlaceables.forEachIndexed { i, placeable -> placeable.place(layout.hostPositions[i]) }
                childPlaceables.forEachIndexed { i, placeable -> placeable.place(layout.childPositions[i]) }
            }
        },
    )
}

/**
 * Группирует детей по хосту, в котором они находятся. Каждому хосту соответствует своя группа (даже если она пуста),
 * а дети без хоста (например добавленные интерцептором) попадают в отдельную группу [ChildGroup.hostIndex] == -1.
 */
private fun buildGroups(hosts: List<NavigationHost>, children: List<UmlTreeNode>): List<ChildGroup> {
    val hostOf: (UmlTreeNode) -> NavigationHost? = { (it.value as? InternalNavigationGraphUmlNode)?.hostInParent }
    val hostGroups = hosts.mapIndexed { index, host ->
        ChildGroup(hostIndex = index, nodes = children.filter { hostOf(it) == host })
    }
    val orphan = children.filter { hostOf(it).let { host -> host == null || host !in hosts } }
    return if (orphan.isEmpty()) hostGroups else hostGroups + ChildGroup(hostIndex = -1, nodes = orphan)
}

private class ChildGroup(val hostIndex: Int, val nodes: List<UmlTreeNode>) {
    fun toMeta(): GroupMeta = GroupMeta(
        hostIndex = hostIndex,
        count = nodes.size,
        // Вертикальная компактная укладка только для хостовых групп из 2+ листьев.
        vertical = hostIndex >= 0 && nodes.size >= 2 && nodes.all { it.children.isEmpty() },
    )
}

private class GroupMeta(val hostIndex: Int, val count: Int, val vertical: Boolean)

/**
 * Вычисляет полный layout ноды: размеры, позиции заголовка/хостов/детей и геометрию линий.
 */
private fun MeasureScope.computeNodeLayout(
    header: Placeable,
    hosts: List<Placeable>,
    children: List<Placeable>,
    groups: List<GroupMeta>,
    dashed: Boolean,
): NodeLayout {
    val padH = CARD_PADDING_H.roundToPx()
    val padV = CARD_PADDING_V.roundToPx()
    val gap = HEADER_TO_HOSTS_GAP.roundToPx()
    val hostSpacing = HOST_SPACING.roundToPx()
    val childrenByGroup = sliceByGroups(children, groups)
    val blocks = groups.mapIndexed { i, meta ->
        buildChildBlock(childrenByGroup[i], meta.vertical, HORIZONTAL_SPACE.roundToPx(), CHILD_INDENT.roundToPx())
    }
    val slots = groups.mapIndexed { i, meta ->
        buildGroupSlot(blocks[i], hostWidthOf(hosts, meta), CONNECTOR_INSET.roundToPx())
    }
    val slotLeft = slotLefts(slots, hostSpacing)

    val hostRowY = padV + header.height + gap
    val hostPositions = hosts.indices.map { hi ->
        val gi = groups.indexOfFirst { it.hostIndex == hi }
        IntOffset(slotLeft[gi] + slots[gi].hostXInSlot, hostRowY)
    }
    val hostBottom = hosts.indices.maxOfOrNull { hostRowY + hosts[it].height } ?: (padV + header.height)
    val cardBottom = if (hosts.isNotEmpty()) hostBottom + padV else padV + header.height + padV

    val card = cardBounds(header, hosts, hostPositions, slots, slotLeft, groups, padH)
    val childrenTop = cardBottom + VERTICAL_SPACE.roundToPx()
    val placed = placeChildren(childrenByGroup, blocks, slots, slotLeft, childrenTop, card, cardBottom)

    val half = max(card.center - placed.minX, placed.maxX - card.center)
    val shiftX = half - card.center
    val segments =
        buildSegments(groups, blocks, slots, slotLeft, hosts, hostRowY, cardBottom, card.center, childrenTop, shiftX)

    return NodeLayout(
        width = 2 * half,
        height = placed.maxBottom,
        headerPosition = IntOffset(card.center - header.width / 2 + shiftX, padV),
        hostPositions = hostPositions.map { IntOffset(it.x + shiftX, it.y) },
        childPositions = placed.positions.map { IntOffset(it.x + shiftX, it.y) },
        lines = NodeLines(
            cardLeft = (card.left + shiftX).toFloat(),
            cardTop = 0f,
            cardRight = (card.right + shiftX).toFloat(),
            cardBottom = cardBottom.toFloat(),
            dashed = dashed,
            segments = segments,
        ),
    )
}

private fun sliceByGroups(children: List<Placeable>, groups: List<GroupMeta>): List<List<Placeable>> {
    var index = 0
    return groups.map { meta -> children.subList(index, index + meta.count).also { index += meta.count } }
}

private fun hostWidthOf(hosts: List<Placeable>, meta: GroupMeta): Int =
    if (meta.hostIndex >= 0) hosts[meta.hostIndex].width else 0

private fun slotLefts(slots: List<GroupSlot>, hostSpacing: Int): IntArray {
    val result = IntArray(slots.size)
    var x = 0
    slots.forEachIndexed { i, slot ->
        result[i] = x
        x += slot.width + hostSpacing
    }
    return result
}

/**
 * Горизонтальные границы карточки: она оборачивает заголовок и строку (разнесённых) хостов, либо, при отсутствии
 * хостов, центрируется над единственной группой детей.
 */
private fun cardBounds(
    header: Placeable,
    hosts: List<Placeable>,
    hostPositions: List<IntOffset>,
    slots: List<GroupSlot>,
    slotLeft: IntArray,
    groups: List<GroupMeta>,
    padH: Int,
): CardBounds {
    val left: Int
    val right: Int
    if (hosts.isNotEmpty()) {
        val minHost = hosts.indices.minOf { hostPositions[it].x }
        val maxHost = hosts.indices.maxOf { hostPositions[it].x + hosts[it].width }
        val center = (minHost + maxHost) / 2
        left = minOf(minHost, center - header.width / 2) - padH
        right = maxOf(maxHost, center + header.width / 2) + padH
    } else {
        val anchor = if (groups.isNotEmpty()) slotLeft[0] + slots[0].anchorXInSlot else header.width / 2
        left = anchor - header.width / 2 - padH
        right = anchor + header.width / 2 + padH
    }
    return CardBounds(left, right)
}

/**
 * Раскладывает детей всех групп в координатах (до центрирующего сдвига) и попутно считает габариты.
 */
private fun placeChildren(
    childrenByGroup: List<List<Placeable>>,
    blocks: List<ChildBlock>,
    slots: List<GroupSlot>,
    slotLeft: IntArray,
    childrenTop: Int,
    card: CardBounds,
    cardBottom: Int,
): PlacedChildren {
    val positions = ArrayList<IntOffset>()
    var minX = card.left
    var maxX = card.right
    var maxBottom = cardBottom
    childrenByGroup.forEachIndexed { gi, groupChildren ->
        val base = slotLeft[gi] + slots[gi].blockLeftInSlot
        groupChildren.forEachIndexed { j, placeable ->
            val offset = blocks[gi].childOffsets[j]
            val x = base + offset.x
            val y = childrenTop + offset.y
            positions += IntOffset(x, y)
            minX = minOf(minX, x)
            maxX = maxOf(maxX, x + placeable.width)
            maxBottom = maxOf(maxBottom, y + placeable.height)
        }
    }
    return PlacedChildren(positions, minX, maxX, maxBottom)
}

/**
 * Строит сегменты соединительных линий (уже со сдвигом [shiftX]) от хостов к их детям.
 */
@Suppress("LongParameterList")
private fun buildSegments(
    groups: List<GroupMeta>,
    blocks: List<ChildBlock>,
    slots: List<GroupSlot>,
    slotLeft: IntArray,
    hosts: List<Placeable>,
    hostRowY: Int,
    cardBottom: Int,
    cardCenter: Int,
    childrenTop: Int,
    shiftX: Int,
): List<Pair<Offset, Offset>> {
    val out = ArrayList<Pair<Offset, Offset>>()
    groups.forEachIndexed { gi, meta ->
        val block = blocks[gi]
        if (block.width == 0) return@forEachIndexed
        val base = slotLeft[gi] + slots[gi].blockLeftInSlot
        val originX = if (meta.hostIndex >= 0) slotLeft[gi] + slots[gi].anchorXInSlot else cardCenter
        val originY = if (meta.hostIndex >= 0) hostRowY + hosts[meta.hostIndex].height else cardBottom
        groupSegments(block.vertical, originX, originY, base, block, childrenTop, shiftX, out)
    }
    return out
}

/** Сегменты линий одной группы: вертикальная шина слева для листьев, либо классический "уголок" для строки. */
@Suppress("LongParameterList")
private fun groupSegments(
    vertical: Boolean,
    originX: Int,
    originY: Int,
    childLeftBase: Int,
    block: ChildBlock,
    childrenTop: Int,
    dx: Int,
    out: MutableList<Pair<Offset, Offset>>,
) {
    if (vertical) {
        val lastCenterY = childrenTop + block.connectYs.last()
        out += seg(originX, originY, originX, lastCenterY, dx)
        block.connectYs.forEachIndexed { i, cy ->
            val y = childrenTop + cy
            out += seg(originX, y, childLeftBase + block.connectXs[i], y, dx)
        }
    } else {
        val midY = (originY + childrenTop) / 2
        val xs = block.connectXs.map { childLeftBase + it }
        out += seg(originX, originY, originX, midY, dx)
        out += seg(minOf(originX, xs.first()), midY, maxOf(originX, xs.last()), midY, dx)
        xs.forEach { out += seg(it, midY, it, childrenTop, dx) }
    }
}

private fun seg(ax: Int, ay: Int, bx: Int, by: Int, dx: Int): Pair<Offset, Offset> =
    Offset((ax + dx).toFloat(), ay.toFloat()) to Offset((bx + dx).toFloat(), by.toFloat())

/**
 * Раскладка детей одной группы. Для вертикальной укладки [anchorXInBlock] указывает на линию-шину (левый край),
 * [connectXs] — на левые края детей; для горизонтальной [anchorXInBlock] — центр блока, [connectXs] — центры детей.
 */
private fun buildChildBlock(
    children: List<Placeable>,
    vertical: Boolean,
    horizontalSpace: Int,
    childIndent: Int,
): ChildBlock {
    if (children.isEmpty()) return ChildBlock(false, 0, 0, emptyList(), emptyList(), emptyList(), 0)
    return if (vertical) {
        val offsets = ArrayList<IntOffset>(children.size)
        val connectXs = ArrayList<Int>(children.size)
        val connectYs = ArrayList<Int>(children.size)
        var y = 0
        children.forEach {
            offsets += IntOffset(childIndent, y)
            connectXs += childIndent
            connectYs += y + it.height / 2
            y += it.height + horizontalSpace
        }
        ChildBlock(
            true,
            childIndent + children.maxOf { it.width },
            y - horizontalSpace,
            offsets,
            connectXs,
            connectYs,
            0,
        )
    } else {
        val offsets = ArrayList<IntOffset>(children.size)
        val connectXs = ArrayList<Int>(children.size)
        var x = 0
        var maxHeight = 0
        children.forEach {
            offsets += IntOffset(x, 0)
            connectXs += x + it.width / 2
            x += it.width + horizontalSpace
            maxHeight = max(maxHeight, it.height)
        }
        val width = x - horizontalSpace
        ChildBlock(false, width, maxHeight, offsets, connectXs, List(children.size) { 0 }, width / 2)
    }
}

/**
 * Слот группы — прямоугольная область, вмещающая карточку хоста и блок его детей, с их взаимным выравниванием.
 */
private fun buildGroupSlot(block: ChildBlock, hostWidth: Int, inset: Int): GroupSlot {
    if (block.width == 0) {
        return GroupSlot(width = hostWidth, hostXInSlot = 0, blockLeftInSlot = 0, anchorXInSlot = hostWidth / 2)
    }
    return if (block.vertical) {
        GroupSlot(
            width = max(hostWidth, inset + block.width),
            hostXInSlot = 0,
            blockLeftInSlot = inset,
            anchorXInSlot = inset + block.anchorXInBlock,
        )
    } else {
        val anchor = block.anchorXInBlock
        val blockLeft = max(0, hostWidth / 2 - anchor)
        val hostX = max(0, anchor - hostWidth / 2)
        GroupSlot(
            width = max(blockLeft + block.width, hostX + hostWidth),
            hostXInSlot = hostX,
            blockLeftInSlot = blockLeft,
            anchorXInSlot = blockLeft + anchor,
        )
    }
}

@Composable
private fun NavGraphCanvas(state: State<NodeLines?>, lineColor: Color, lineWidth: Dp) {
    val cardColor = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(Modifier) {
        val lines by state
        val spec = lines ?: return@Canvas
        val corner = CornerRadius(CARD_CORNER.toPx())
        val topLeft = Offset(spec.cardLeft, spec.cardTop)
        val size = Size(spec.cardRight - spec.cardLeft, spec.cardBottom - spec.cardTop)
        drawRoundRect(color = cardColor, topLeft = topLeft, size = size, cornerRadius = corner)
        val stroke = if (spec.dashed) {
            Stroke(
                width = DASH_BORDER_WIDTH.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH_ON_PX, DASH_OFF_PX)),
            )
        } else {
            Stroke(width = BORDER_WIDTH.toPx())
        }
        drawRoundRect(color = borderColor, topLeft = topLeft, size = size, cornerRadius = corner, style = stroke)
        val strokeWidthPx = lineWidth.toPx()
        spec.segments.forEach { (start, end) ->
            drawLine(color = lineColor, start = start, end = end, strokeWidth = strokeWidthPx, cap = StrokeCap.Round)
        }
    }
}

private class ChildBlock(
    val vertical: Boolean,
    val width: Int,
    val height: Int,
    val childOffsets: List<IntOffset>,
    val connectXs: List<Int>,
    val connectYs: List<Int>,
    val anchorXInBlock: Int,
)

private class GroupSlot(val width: Int, val hostXInSlot: Int, val blockLeftInSlot: Int, val anchorXInSlot: Int)

private class CardBounds(val left: Int, val right: Int) {
    val center: Int get() = (left + right) / 2
}

private class PlacedChildren(val positions: List<IntOffset>, val minX: Int, val maxX: Int, val maxBottom: Int)

private class NodeLayout(
    val width: Int,
    val height: Int,
    val headerPosition: IntOffset,
    val hostPositions: List<IntOffset>,
    val childPositions: List<IntOffset>,
    val lines: NodeLines,
)

private class NodeLines(
    val cardLeft: Float,
    val cardTop: Float,
    val cardRight: Float,
    val cardBottom: Float,
    val dashed: Boolean,
    val segments: List<Pair<Offset, Offset>>,
)
