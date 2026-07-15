package ru.vladislavsumin.core.coroutines.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CombineExtTest {
    @Test
    fun testCombine6PreservesArgumentOrder() = runTest {
        val result = combine(
            flowOf(1),
            flowOf("2"),
            flowOf(3L),
            flowOf(4.0),
            flowOf(true),
            flowOf('6'),
        ) { a1, a2, a3, a4, a5, a6 ->
            listOf(a1, a2, a3, a4, a5, a6)
        }.first()

        assertEquals(listOf<Any?>(1, "2", 3L, 4.0, true, '6'), result)
    }

    @Test
    fun testCombine7PreservesArgumentOrder() = runTest {
        val result = combine(
            flowOf(1),
            flowOf("2"),
            flowOf(3L),
            flowOf(4.0),
            flowOf(true),
            flowOf('6'),
            flowOf(7u),
        ) { a1, a2, a3, a4, a5, a6, a7 ->
            listOf(a1, a2, a3, a4, a5, a6, a7)
        }.first()

        assertEquals(listOf<Any?>(1, "2", 3L, 4.0, true, '6', 7u), result)
    }

    @Test
    fun testCombine8PreservesArgumentOrder() = runTest {
        val result = combine(
            flowOf(1),
            flowOf("2"),
            flowOf(3L),
            flowOf(4.0),
            flowOf(true),
            flowOf('6'),
            flowOf(7u),
            flowOf(8.toShort()),
        ) { a1, a2, a3, a4, a5, a6, a7, a8 ->
            listOf(a1, a2, a3, a4, a5, a6, a7, a8)
        }.first()

        assertEquals(listOf<Any?>(1, "2", 3L, 4.0, true, '6', 7u, 8.toShort()), result)
    }

    @Test
    fun testCombine9PreservesArgumentOrder() = runTest {
        val result = combine(
            flowOf(1),
            flowOf("2"),
            flowOf(3L),
            flowOf(4.0),
            flowOf(true),
            flowOf('6'),
            flowOf(7u),
            flowOf(8.toShort()),
            flowOf(9.toByte()),
        ) { a1, a2, a3, a4, a5, a6, a7, a8, a9 ->
            listOf(a1, a2, a3, a4, a5, a6, a7, a8, a9)
        }.first()

        assertEquals(listOf<Any?>(1, "2", 3L, 4.0, true, '6', 7u, 8.toShort(), 9.toByte()), result)
    }

    @Test
    fun testCombineEmitsOnEachUpdate() = runTest {
        val flow = MutableStateFlow(0)
        val results = mutableListOf<String>()

        val job = launch {
            combine(
                flow,
                flowOf("a"),
                flowOf("b"),
                flowOf("c"),
                flowOf("d"),
                flowOf("e"),
            ) { a1, a2, a3, a4, a5, a6 ->
                listOf(a1, a2, a3, a4, a5, a6).joinToString(separator = "")
            }.collect { results += it }
        }
        runCurrent()

        flow.value = 1
        runCurrent()

        job.cancel()
        assertEquals(listOf("0abcde", "1abcde"), results)
    }
}
