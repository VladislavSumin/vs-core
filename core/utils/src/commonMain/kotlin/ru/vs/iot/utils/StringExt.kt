package ru.vs.iot.utils

fun String.decapitalized(): String {
    return this.replaceFirstChar { it.lowercase() }
}
