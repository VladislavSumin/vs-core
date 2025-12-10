package ru.vladislavsumin.core.coroutines.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual fun getDefaultIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
