package ru.vladislavsumin.core.decompose.components.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FlowToValueTest {
    @Test
    fun testSimpleValue() = runTest {
        val flow = MutableStateFlow(0)
        val value = flow.asValue(this)
        assertEquals(0, value.value)
    }

    @Test
    fun testChangeSimpleValue() = runTest {
        val flow = MutableStateFlow(0)
        val value = flow.asValue(this)
        flow.value = 2
        assertEquals(2, value.value)
    }

    @Test
    fun testSubscribeEmitCurrent() = runTest {
        val flow = MutableStateFlow(0)
        val value = flow.asValue(this)
        var data = -1
        val cancellation = value.subscribe { data = it }
        runCurrent()
        cancellation.cancel()
        assertEquals(0, data)
    }

    @Test
    fun testSubscribeEmitSequence() = runTest {
        val flow = MutableStateFlow(0)
        val value = flow.asValue(this)
        var data = mutableListOf<Int>()
        val cancellation = value.subscribe { data += it }
        runCurrent()

        flow.value = 1
        runCurrent()

        cancellation.cancel()

        assertEquals(listOf(0, 1), data)
    }

    @Test
    fun testSubscribeNotEmitAfterCancel() = runTest {
        val flow = MutableStateFlow(0)
        val value = flow.asValue(this)
        var data = mutableListOf<Int>()
        val cancellation = value.subscribe { data += it }
        runCurrent()

        cancellation.cancel()
        flow.value = 1
        runCurrent()

        assertEquals(listOf(0), data)
    }
}
