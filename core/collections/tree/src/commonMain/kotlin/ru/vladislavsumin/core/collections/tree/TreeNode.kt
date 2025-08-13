package ru.vladislavsumin.core.collections.tree

/**
 * Лист дерева, одновременно представляет собой все дерево, где текущий лист является корнем.
 *
 * @property value значение листа дерева.
 * @property children дочерние листы дерева.
 */
public interface TreeNode<T, N : TreeNode<T, N>> {
    public val value: T
    public val children: Collection<N>
}

public class TreeNodeImpl<T> internal constructor(
    override val value: T,
    override val children: Collection<TreeNodeImpl<T>>,
) : TreeNode<T, TreeNodeImpl<T>>

/**
 * DSL для построения деревьев.
 */
public fun <T> nodeOf(
    value: T,
    vararg children: TreeNodeImpl<T>,
): TreeNodeImpl<T> = TreeNodeImpl(value, children.toList())

/**
 * Итератор по всем [TreeNode.children] начиная с детей и заканчивая внуками и далее.
 *
 * @param branchFilter дает возможность исключать из обхода ветки целиком.
 */
public fun <T, N : TreeNode<T, N>> N.asSequence(branchFilter: (N) -> Boolean = { true }): Sequence<N> = sequence {
    val root = this@asSequence
    if (branchFilter(root)) {
        yield(root)
    } else {
        return@sequence
    }
    var children = root.children
    while (children.isNotEmpty()) {
        val newChildren = mutableListOf<N>()
        children.forEach { node ->
            if (branchFilter(node)) {
                yield(node)
                newChildren += node.children
            }
        }
        children = newChildren
    }
}

/**
 * Ищет [TreeNode] по переданному пути, начиная от текущей (включительно).
 */
public fun <T, K, N : TreeNode<T, N>> N.findByPath(path: List<K>, keySelector: (T) -> K): N? {
    // Если путь пустой, то нечего искать.
    if (path.isEmpty()) return null

    // Проверяем что путь действительно начинается от корня.
    if (path.first() != keySelector(value)) return null

    // Если путь длинной один, то искомая нода текущая.
    if (path.size == 1) return this

    var node = this
    path
        .asSequence()
        .drop(1) // Пропускаем корень, который проверили ранее.
        .forEach { element ->
            node = node.children.find { node -> keySelector(node.value) == element } ?: return null
        }

    return node
}

public fun <T, V, N : TreeNode<T, N>> N.map(mapper: (T) -> V): TreeNodeImpl<V> {
    return TreeNodeImpl(
        value = mapper(value),
        children = children.map { it.map(mapper) },
    )
}
