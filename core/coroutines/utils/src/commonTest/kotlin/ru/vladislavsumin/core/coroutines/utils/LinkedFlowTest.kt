package ru.vladislavsumin.core.coroutines.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class LinkedFlowTest {
    @Test
    fun testEmitsValuesFromLinkedSource() = runTest {
        val linkedFlow = LinkedFlow<Int>()
        flowOf(1, 2, 3).linkTo(linkedFlow)

        val results = mutableListOf<Int>()
        val job = launch { linkedFlow.collect { results += it } }
        runCurrent()

        job.cancel()
        assertEquals(listOf(1, 2, 3), results)
    }

    @Test
    fun testCollectBeforeLinkReceivesValuesAfterLink() = runTest {
        val linkedFlow = LinkedFlow<Int>()

        val results = mutableListOf<Int>()
        val job = launch { linkedFlow.collect { results += it } }
        runCurrent()
        assertEquals(emptyList(), results)

        val source = MutableSharedFlow<Int>()
        source.linkTo(linkedFlow)
        runCurrent()

        source.emit(1)
        runCurrent()

        job.cancel()
        assertEquals(listOf(1), results)
    }

    @Test
    fun testMultipleCollectorsReceiveValues() = runTest {
        val linkedFlow = LinkedFlow<Int>()
        val source = MutableStateFlow(0)
        source.linkTo(linkedFlow)

        val results1 = mutableListOf<Int>()
        val results2 = mutableListOf<Int>()
        val job1 = launch { linkedFlow.collect { results1 += it } }
        val job2 = launch { linkedFlow.collect { results2 += it } }
        runCurrent()

        source.value = 1
        runCurrent()

        job1.cancel()
        job2.cancel()
        assertEquals(listOf(0, 1), results1)
        assertEquals(listOf(0, 1), results2)
    }

    @Test
    fun testSecondLinkFails() {
        val linkedFlow = LinkedFlow<Int>()
        flowOf(1).linkTo(linkedFlow)

        assertFailsWith<IllegalStateException> {
            flowOf(2).linkTo(linkedFlow)
        }
    }
}
