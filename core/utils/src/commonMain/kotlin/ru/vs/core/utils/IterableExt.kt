package ru.vs.core.utils

inline fun <T> Iterable<T>.forEachApply(action: T.() -> Unit) {
    for (element in this) action(element)
}
