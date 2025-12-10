package ru.vladislavsumin.core.coroutines.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal actual fun getDefaultIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
