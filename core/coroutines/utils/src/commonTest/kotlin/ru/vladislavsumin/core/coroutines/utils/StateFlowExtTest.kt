package ru.vladislavsumin.core.coroutines.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class StateFlowExtTest {
    @Test
    fun testValueReflectsCurrentSourceValue() {
        val source = MutableStateFlow(1)
        val mapped = source.mapState { it * 2 }

        assertEquals(2, mapped.value)

        source.value = 3
        assertEquals(6, mapped.value)
    }

    @Test
    fun testReplayCacheContainsCurrentValue() {
        val source = MutableStateFlow(1)
        val mapped = source.mapState { it * 2 }

        assertEquals(listOf(2), mapped.replayCache)

        source.value = 3
        assertEquals(listOf(6), mapped.replayCache)
    }

    @Test
    fun testCollectEmitsTransformedValues() = runTest {
        val source = MutableStateFlow(1)
        val mapped = source.mapState { it * 2 }

        val results = mutableListOf<Int>()
        val job = launch { mapped.collect { results += it } }
        runCurrent()

        source.value = 2
        runCurrent()
        source.value = 3
        runCurrent()

        job.cancel()
        assertEquals(listOf(2, 4, 6), results)
    }

    @Test
    fun testCollectSkipsEqualTransformedValues() = runTest {
        val source = MutableStateFlow(1)
        val mapped = source.mapState { it > 0 }

        val results = mutableListOf<Boolean>()
        val job = launch { mapped.collect { results += it } }
        runCurrent()

        source.value = 2
        runCurrent()
        source.value = -1
        runCurrent()

        job.cancel()
        assertEquals(listOf(true, false), results)
    }
}
