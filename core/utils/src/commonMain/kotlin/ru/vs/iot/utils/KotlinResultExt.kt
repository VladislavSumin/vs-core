package ru.vs.iot.utils

inline fun <T> Result<T>.onException(action: (exception: Exception) -> Unit): Result<T> = onFailure {
    when (it) {
        is Exception -> action(it)
        else -> throw it
    }
}
