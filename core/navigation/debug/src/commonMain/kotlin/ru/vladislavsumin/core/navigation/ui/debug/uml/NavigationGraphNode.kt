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

/** Радиус точки в месте соединения линии с хостом или дочерней нодой. */
private val DOT_RADIUS = 3.dp

private const val DASH_ON_PX = 16f
private const val DASH_OFF_PX = 8f

/** Шаг дискретизации силуэта (контура) поддерева по вертикали, в пикселях. */
private const val CONTOUR_STEP = 4

/**
 * Минимальный вертикальный запас между нодами соседних поддеревьев. При укладке по контурам содержимое,
 * находящееся в пределах этого запаса по вертикали, считается пересекающимся, поэтому диагонально соседствующие
 * ноды дополнительно разъезжаются по горизонтали и не теснятся друг к другу.
 */
private val CONTOUR_VERTICAL_MARGIN = 12.dp

/**
 * Отрисовывает навигационный граф с группировкой дочерних экранов по [NavigationHost].
 *
 * Каждая нода рисуется как карточка, внутри которой находится заголовок экрана и карточки его хостов навигации.
 * Дочерние экраны группируются по хосту, в котором они объявлены ([InternalNavigationGraphUmlNode.hostInParent]),
 * и располагаются под соответствующим хостом. Соединительные линии идут от конкретной карточки хоста к его дочерним
 * экранам. Если все дети одного хоста — листья, они укладываются компактно вертикально (как в [CompactTree]),
 * иначе горизонтально в строку.
 *
 * Для плотной укладки соседние поддеревья разносятся не по ширине их прямоугольных габаритов, а по силуэтам
 * (контурам): узкое неглубокое поддерево "вкладывается" в свободное место рядом с широким ветвистым соседом.
 * Контур поддерева вычисляется на этапе measure и передаётся вверх родителю через [contourSink].
 */
@Composable
internal fun NavigationGraphNode(
    node: UmlTreeNode,
    lineColor: Color,
    modifier: Modifier = Modifier,
    lineWidth: Dp = 1.dp,
    contourSink: ContourSink? = null,
) {
    val info = node.value
    val hosts = (info as? InternalNavigationGraphUmlNode)?.navigationHosts?.toList().orEmpty()
    val children = node.children.toList()
    val groups = buildGroups(hosts, children)
    val orderedChildren = groups.flatMap { it.nodes }
    val drawState = remember { mutableStateOf<NodeLines?>(null) }
    val childSinks = remember(node) { List(orderedChildren.size) { ContourSink() } }

    Layout(
        content = {
            NavGraphCanvas(drawState, lineColor, lineWidth)
            NavigationNodeHeader(info)
            hosts.forEach { NavigationHostCard(it) }
            orderedChildren.forEachIndexed { i, child ->
                NavigationGraphNode(child, lineColor, lineWidth = lineWidth, contourSink = childSinks[i])
            }
        },
        modifier = modifier,
        measurePolicy = { measurables, _ ->
            val infinite = Constraints()
            val header = measurables[1].measure(infinite)
            val hostPlaceables = hosts.indices.map { measurables[2 + it].measure(infinite) }
            val childPlaceables = measurables.drop(2 + hosts.size).map { it.measure(infinite) }
            val childContours = childSinks.map { it.contour }
            val childCardCenters = childSinks.map { it.cardCenterX }
            val metas = groups.map { it.toMeta() }

            val layout =
                computeNodeLayout(
                    header,
                    hostPlaceables,
                    childPlaceables,
                    childContours,
                    childCardCenters,
                    metas,
                    !info.isPartOfMainGraph,
                )
            val canvas = measurables[0].measure(Constraints.fixed(layout.width, layout.height))
            drawState.value = layout.lines
            contourSink?.let {
                it.contour = layout.contour
                it.cardCenterX = layout.cardCenterX
            }

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
 * Вычисляет полный layout ноды: размеры, позиции заголовка/хостов/детей, геометрию линий и контур поддерева.
 */
@Suppress("LongMethod")
private fun MeasureScope.computeNodeLayout(
    header: Placeable,
    hosts: List<Placeable>,
    children: List<Placeable>,
    childContours: List<Contour>,
    childCardCenters: List<Int>,
    groups: List<GroupMeta>,
    dashed: Boolean,
): NodeLayout {
    val padV = CARD_PADDING_V.roundToPx()
    val verticalMarginBands = CONTOUR_VERTICAL_MARGIN.roundToPx() / CONTOUR_STEP
    val childrenByGroup = sliceByGroups(children, groups)
    val contoursByGroup = sliceByGroups(childContours, groups)
    val cardCentersByGroup = sliceByGroups(childCardCenters, groups)
    val blocks = groups.mapIndexed { i, meta ->
        buildChildBlock(
            childrenByGroup[i],
            contoursByGroup[i],
            cardCentersByGroup[i],
            meta.vertical,
            HORIZONTAL_SPACE.roundToPx(),
            CHILD_INDENT.roundToPx(),
            verticalMarginBands,
        )
    }
    val slots = groups.mapIndexed { i, meta ->
        buildGroupSlot(
            blocks[i],
            hostWidthOf(hosts, meta),
            CONNECTOR_INSET.roundToPx(),
        )
    }

    val hostRowY = padV + header.height + HEADER_TO_HOSTS_GAP.roundToPx()
    val hostBottom = hosts.indices.maxOfOrNull { hostRowY + hosts[it].height } ?: (padV + header.height)
    val cardBottom = if (hosts.isNotEmpty()) hostBottom + padV else padV + header.height + padV
    val childrenTop = cardBottom + VERTICAL_SPACE.roundToPx()

    val slotContours = groups.mapIndexed { i, meta ->
        buildSlotContour(
            slots[i],
            blocks[i],
            hostHeightOf(hosts, meta),
            hostWidthOf(hosts, meta),
            childrenTop - hostRowY,
        )
    }
    val slotLeft = packSlots(slotContours, HOST_SPACING.roundToPx(), verticalMarginBands)

    val hostPositions = hosts.indices.map { hi ->
        val gi = groups.indexOfFirst { it.hostIndex == hi }
        IntOffset(slotLeft[gi] + slots[gi].hostXInSlot, hostRowY)
    }
    val card = cardBounds(header, hosts, hostPositions, slots, slotLeft, groups, CARD_PADDING_H.roundToPx())
    val placed = placeChildren(childrenByGroup, blocks, slots, slotLeft, childrenTop, card, cardBottom)

    // Бокс = точные габариты контента (без симметричного центрирования): экономит ширину. Родитель соединяется
    // с реальным центром карточки ребёнка через выставляемый cardCenterX, а не с центром бокса.
    val shiftX = -placed.minX
    val width = placed.maxX - placed.minX
    val childPositions = placed.positions.map { IntOffset(it.x + shiftX, it.y) }
    val connectors =
        buildSegments(groups, blocks, slots, slotLeft, hosts, hostRowY, cardBottom, card.center, childrenTop, shiftX)
    val contour =
        buildNodeContour(
            card.left + shiftX,
            card.right + shiftX,
            cardBottom,
            childPositions,
            childContours,
            connectors.segments,
            placed.maxBottom,
        )

    return NodeLayout(
        width = width,
        height = placed.maxBottom,
        cardCenterX = card.center + shiftX,
        headerPosition = IntOffset(card.center - header.width / 2 + shiftX, padV),
        hostPositions = hostPositions.map { IntOffset(it.x + shiftX, it.y) },
        childPositions = childPositions,
        lines = NodeLines(
            cardLeft = (card.left + shiftX).toFloat(),
            cardTop = 0f,
            cardRight = (card.right + shiftX).toFloat(),
            cardBottom = cardBottom.toFloat(),
            dashed = dashed,
            segments = connectors.segments,
            dots = connectors.dots,
        ),
        contour = contour,
    )
}

private fun <T> sliceByGroups(items: List<T>, groups: List<GroupMeta>): List<List<T>> {
    var index = 0
    return groups.map { meta -> items.subList(index, index + meta.count).also { index += meta.count } }
}

private fun hostWidthOf(hosts: List<Placeable>, meta: GroupMeta): Int =
    if (meta.hostIndex >= 0) hosts[meta.hostIndex].width else 0

private fun hostHeightOf(hosts: List<Placeable>, meta: GroupMeta): Int =
    if (meta.hostIndex >= 0) hosts[meta.hostIndex].height else 0

/**
 * Раскладывает слоты хостов слева направо, разнося их по контурам (силуэтам), а не по прямоугольным габаритам.
 */
private fun packSlots(slotContours: List<Contour>, spacing: Int, verticalMarginBands: Int): IntArray {
    val result = IntArray(slotContours.size)
    var running = IntArray(0)
    slotContours.forEachIndexed { i, contour ->
        val dx = separation(running, contour, spacing, verticalMarginBands)
        result[i] = dx
        running = mergeRight(running, contour, dx)
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
 * Строит сегменты соединительных линий и точки соединения (уже со сдвигом [shiftX]) от хостов к их детям.
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
): Connectors {
    val segments = ArrayList<Pair<Offset, Offset>>()
    val dots = ArrayList<Offset>()
    groups.forEachIndexed { gi, meta ->
        val block = blocks[gi]
        if (block.width == 0) return@forEachIndexed
        val base = slotLeft[gi] + slots[gi].blockLeftInSlot
        val originX = if (meta.hostIndex >= 0) slotLeft[gi] + slots[gi].anchorXInSlot else cardCenter
        val originY = if (meta.hostIndex >= 0) hostRowY + hosts[meta.hostIndex].height else cardBottom
        // Точка в месте соединения линии с хостом навигации.
        if (meta.hostIndex >= 0) dots += Offset((originX + shiftX).toFloat(), originY.toFloat())
        groupSegments(block.vertical, originX, originY, base, block, childrenTop, shiftX, segments, dots)
    }
    return Connectors(segments, dots)
}

/** Сегменты и точки одной группы: вертикальная шина слева для листьев, либо классический "уголок" для строки. */
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
    dots: MutableList<Offset>,
) {
    if (vertical) {
        val lastCenterY = childrenTop + block.connectYs.last()
        out += seg(originX, originY, originX, lastCenterY, dx)
        block.connectYs.forEachIndexed { i, cy ->
            val y = childrenTop + cy
            val childX = childLeftBase + block.connectXs[i]
            out += seg(originX, y, childX, y, dx)
            dots += Offset((childX + dx).toFloat(), y.toFloat())
        }
    } else {
        val midY = (originY + childrenTop) / 2
        val xs = block.connectXs.map { childLeftBase + it }
        out += seg(originX, originY, originX, midY, dx)
        out += seg(minOf(originX, xs.first()), midY, maxOf(originX, xs.last()), midY, dx)
        xs.forEach {
            out += seg(it, midY, it, childrenTop, dx)
            dots += Offset((it + dx).toFloat(), childrenTop.toFloat())
        }
    }
}

private fun seg(ax: Int, ay: Int, bx: Int, by: Int, dx: Int): Pair<Offset, Offset> =
    Offset((ax + dx).toFloat(), ay.toFloat()) to Offset((bx + dx).toFloat(), by.toFloat())

/**
 * Раскладка детей одной группы: горизонтально (соседи разносятся по контурам) либо вертикально (листья столбцом).
 *
 * Для вертикальной укладки [ChildBlock.anchorXInBlock] указывает на линию-шину (левый край), [ChildBlock.connectXs] —
 * на левые края детей; для горизонтальной [ChildBlock.anchorXInBlock] — точка привязки хоста, [ChildBlock.connectXs] —
 * центры детей.
 */
private fun buildChildBlock(
    children: List<Placeable>,
    contours: List<Contour>,
    cardCenters: List<Int>,
    vertical: Boolean,
    horizontalSpace: Int,
    childIndent: Int,
    verticalMarginBands: Int,
): ChildBlock {
    if (children.isEmpty()) {
        return ChildBlock(false, 0, 0, emptyList(), emptyList(), emptyList(), 0, EMPTY_CONTOUR)
    }
    return if (vertical) {
        buildVerticalBlock(children, contours, horizontalSpace, childIndent)
    } else {
        buildHorizontalBlock(children, contours, cardCenters, horizontalSpace, verticalMarginBands)
    }
}

private fun buildHorizontalBlock(
    children: List<Placeable>,
    contours: List<Contour>,
    cardCenters: List<Int>,
    horizontalSpace: Int,
    verticalMarginBands: Int,
): ChildBlock {
    val offsets = ArrayList<IntOffset>(children.size)
    val connectXs = ArrayList<Int>(children.size)
    var running = IntArray(0)
    var maxRight = 0
    var maxHeight = 0
    children.forEachIndexed { i, placeable ->
        val dx = separation(running, contours[i], horizontalSpace, verticalMarginBands)
        offsets += IntOffset(dx, 0)
        // Соединяемся с реальным центром карточки ребёнка, а не с центром его бокса.
        connectXs += dx + cardCenters[i]
        running = mergeRight(running, contours[i], dx)
        maxRight = max(maxRight, dx + placeable.width)
        maxHeight = max(maxHeight, placeable.height)
    }
    val builder = ContourBuilder(maxHeight)
    offsets.forEachIndexed { i, offset -> builder.stampContour(contours[i], offset.x, offset.y) }
    val anchor = (connectXs.first() + connectXs.last()) / 2
    return ChildBlock(
        false,
        maxRight,
        maxHeight,
        offsets,
        connectXs,
        List(children.size) { 0 },
        anchor,
        builder.build(),
    )
}

private fun buildVerticalBlock(
    children: List<Placeable>,
    contours: List<Contour>,
    horizontalSpace: Int,
    childIndent: Int,
): ChildBlock {
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
    val height = y - horizontalSpace
    val width = childIndent + children.maxOf { it.width }
    val builder = ContourBuilder(height)
    offsets.forEachIndexed { i, offset -> builder.stampContour(contours[i], offset.x, offset.y) }
    return ChildBlock(true, width, height, offsets, connectXs, connectYs, 0, builder.build())
}

/**
 * Слот группы — область, вмещающая карточку хоста и блок его детей, с их взаимным выравниванием.
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

/** Контур слота: силуэт карточки хоста плюс силуэт блока его детей ниже. */
private fun buildSlotContour(
    slot: GroupSlot,
    block: ChildBlock,
    hostHeight: Int,
    hostWidth: Int,
    blockTopInSlot: Int,
): Contour {
    val height = max(if (hostWidth > 0) hostHeight else 0, blockTopInSlot + block.height)
    val builder = ContourBuilder(height)
    if (hostWidth > 0) builder.stampRect(slot.hostXInSlot, 0, slot.hostXInSlot + hostWidth, hostHeight)
    builder.stampContour(block.contour, slot.blockLeftInSlot, blockTopInSlot)
    return builder.build()
}

/**
 * Контур всего поддерева: силуэт своей карточки, силуэты дочерних поддеревьев на их местах и собственные
 * соединительные линии (чтобы соседние поддеревья не наезжали на эти линии при укладке).
 */
private fun buildNodeContour(
    cardLeft: Int,
    cardRight: Int,
    cardBottom: Int,
    childPositions: List<IntOffset>,
    childContours: List<Contour>,
    segments: List<Pair<Offset, Offset>>,
    height: Int,
): Contour {
    val builder = ContourBuilder(height)
    builder.stampRect(cardLeft, 0, cardRight, cardBottom)
    childContours.forEachIndexed { i, contour ->
        builder.stampContour(
            contour,
            childPositions[i].x,
            childPositions[i].y,
        )
    }
    segments.forEach { (start, end) -> builder.stampSegment(start.x, start.y, end.x, end.y) }
    return builder.build()
}

/**
 * Минимальный сдвиг вправо для контура [next], при котором его левый силуэт не пересекается с правым силуэтом уже
 * разложенных элементов [running] (плюс зазор [spacing]).
 *
 * Правый силуэт [running] дополнительно «раздувается» по вертикали на [verticalMarginBands] полос: содержимое,
 * находящееся в пределах этого запаса по вертикали, тоже учитывается. Благодаря этому диагонально соседствующие
 * ноды получают горизонтальный отступ и не теснятся друг к другу.
 */
private fun separation(running: IntArray, next: Contour, spacing: Int, verticalMarginBands: Int): Int {
    var dx = 0
    for (b in next.left.indices) {
        val nextLeft = next.left[b]
        if (nextLeft == Int.MAX_VALUE) continue
        val from = (b - verticalMarginBands).coerceAtLeast(0)
        val to = (b + verticalMarginBands).coerceAtMost(running.size - 1)
        var runningRight = Int.MIN_VALUE
        for (rb in from..to) {
            if (running[rb] > runningRight) runningRight = running[rb]
        }
        if (runningRight != Int.MIN_VALUE) {
            val need = runningRight - nextLeft + spacing
            if (need > dx) dx = need
        }
    }
    return dx
}

/** Добавляет к правому силуэту [running] правый силуэт контура [next], размещённого со сдвигом [dx]. */
private fun mergeRight(running: IntArray, next: Contour, dx: Int): IntArray {
    val size = maxOf(running.size, next.right.size)
    val result = IntArray(size) { if (it < running.size) running[it] else Int.MIN_VALUE }
    next.right.indices.forEach { b ->
        if (next.right[b] == Int.MIN_VALUE) return@forEach
        val value = next.right[b] + dx
        if (value > result[b]) result[b] = value
    }
    return result
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
        val dotRadius = DOT_RADIUS.toPx()
        spec.dots.forEach { center -> drawCircle(color = lineColor, radius = dotRadius, center = center) }
    }
}

/**
 * Силуэт (контур) поддерева относительно его бокса. [left]/[right] — крайние x на каждой горизонтальной полосе
 * высотой [CONTOUR_STEP]; пустые полосы помечены [Int.MAX_VALUE]/[Int.MIN_VALUE].
 */
internal class Contour(val left: IntArray, val right: IntArray)

private val EMPTY_CONTOUR = Contour(IntArray(0), IntArray(0))

/** Приёмник контура: заполняется дочерней нодой на этапе measure и читается родителем. */
internal class ContourSink {
    var contour: Contour = EMPTY_CONTOUR
    var cardCenterX: Int = 0
}

/** Построитель контура: "штампует" в него прямоугольники и вложенные контуры. */
private class ContourBuilder(heightPx: Int) {
    private val bandCount = if (heightPx <= 0) 0 else (heightPx + CONTOUR_STEP - 1) / CONTOUR_STEP
    private val left = IntArray(bandCount) { Int.MAX_VALUE }
    private val right = IntArray(bandCount) { Int.MIN_VALUE }

    fun stampRect(xLeft: Int, yTop: Int, xRight: Int, yBottom: Int) {
        forBands(yTop, yBottom) { b ->
            if (xLeft < left[b]) left[b] = xLeft
            if (xRight > right[b]) right[b] = xRight
        }
    }

    /** Штампует отрезок соединительной линии (в тех же координатах, что и контур) как тонкий прямоугольник. */
    fun stampSegment(ax: Float, ay: Float, bx: Float, by: Float) {
        val xLeft = minOf(ax, bx).toInt()
        val xRight = maxOf(ax, bx).toInt()
        val yTop = minOf(ay, by).toInt()
        // +1, чтобы горизонтальный отрезок (нулевой высоты) попал хотя бы в одну полосу дискретизации.
        val yBottom = maxOf(ay, by).toInt() + 1
        stampRect(xLeft, yTop, xRight, yBottom)
    }

    fun stampContour(contour: Contour, dx: Int, dy: Int) {
        contour.left.indices.forEach { b ->
            if (contour.left[b] == Int.MAX_VALUE) return@forEach
            val newLeft = contour.left[b] + dx
            val newRight = contour.right[b] + dx
            val yTop = dy + b * CONTOUR_STEP
            forBands(yTop, yTop + CONTOUR_STEP) { tb ->
                if (newLeft < left[tb]) left[tb] = newLeft
                if (newRight > right[tb]) right[tb] = newRight
            }
        }
    }

    fun build(): Contour = Contour(left, right)

    private inline fun forBands(yTop: Int, yBottom: Int, action: (Int) -> Unit) {
        if (bandCount == 0) return
        val first = (yTop / CONTOUR_STEP).coerceIn(0, bandCount - 1)
        val last = ((yBottom - 1) / CONTOUR_STEP).coerceIn(0, bandCount - 1)
        for (b in first..last) action(b)
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
    val contour: Contour,
)

private class GroupSlot(val width: Int, val hostXInSlot: Int, val blockLeftInSlot: Int, val anchorXInSlot: Int)

private class CardBounds(val left: Int, val right: Int) {
    val center: Int get() = (left + right) / 2
}

private class PlacedChildren(val positions: List<IntOffset>, val minX: Int, val maxX: Int, val maxBottom: Int)

private class NodeLayout(
    val width: Int,
    val height: Int,
    val cardCenterX: Int,
    val headerPosition: IntOffset,
    val hostPositions: List<IntOffset>,
    val childPositions: List<IntOffset>,
    val lines: NodeLines,
    val contour: Contour,
)

private class NodeLines(
    val cardLeft: Float,
    val cardTop: Float,
    val cardRight: Float,
    val cardBottom: Float,
    val dashed: Boolean,
    val segments: List<Pair<Offset, Offset>>,
    val dots: List<Offset>,
)

private class Connectors(val segments: List<Pair<Offset, Offset>>, val dots: List<Offset>)
