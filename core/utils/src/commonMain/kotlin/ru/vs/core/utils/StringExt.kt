package ru.vs.core.utils

fun String.decapitalized(): String {
    return this.replaceFirstChar { it.lowercase() }
}
