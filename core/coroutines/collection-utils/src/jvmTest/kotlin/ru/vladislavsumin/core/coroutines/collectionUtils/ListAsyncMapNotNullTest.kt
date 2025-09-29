package ru.vladislavsumin.core.coroutines.collectionUtils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class ListAsyncMapNotNullTest {
    @Test
    fun mapCollectionWithoutNulls() = runBlocking {
        val original = buildList(ELEMENT_COUNT) { repeat(ELEMENT_COUNT) { add(it) } }
        val expected = original.map { -it }
        runAll("mapCollectionWithoutNulls", original, expected) { -it }
    }

    @Test
    fun mapCollectionWitNulls() = runBlocking {
        val original = buildList(ELEMENT_COUNT) { repeat(ELEMENT_COUNT) { add(it) } }
        val expected = original.mapNotNull { if (it % 2 == 0) it else null }
        runAll("mapCollectionWitNulls", original, expected) { if (it % 2 == 0) it else null }
    }

    @Test
    fun mapCpuIntensiveWitNulls() = runBlocking {
        val original = buildList(ELEMENT_COUNT) { repeat(ELEMENT_COUNT) { add(it) } }
        val expected = original.mapNotNull { if (it % 2 == 0) it else null }
        runAll("mapCpuIntensiveWitNulls", original, expected) {
            repeat(100_000) {
                repeat(10) {
                    it * it + it / sqrt(it.toDouble())
                }
            }
            if (it % 2 == 0) it else null
        }
    }

    private suspend inline fun <T : Any, R : Any> runAll(
        name: String,
        original: List<T>,
        expected: List<R>,
        noinline map: (T) -> R?,
    ) = withContext(Dispatchers.Default) {
        println("=======================================")
        runSingleThread(name, original, expected, map)
        runAsyncMapNotNull(name, original, expected, map)
        runParallelStreams(name, original, expected, map)
        runAsyncJobs(name, original, expected, map)
        println("=======================================")
    }

    private suspend inline fun <T : Any, R : Any> runSingleThread(
        name: String,
        original: List<T>,
        expected: List<R>,
        noinline map: (T) -> R?,
    ) {
        val duration = runTest(REPEAT_COUNT, expected) { original.mapNotNull(transform = map) }
        println("$name#singleThread = $duration")
    }

    private suspend inline fun <T : Any, R : Any> runAsyncMapNotNull(
        name: String,
        original: List<T>,
        expected: List<R>,
        noinline map: (T) -> R?,
    ) {
        val duration = runTest(REPEAT_COUNT, expected) {
            original.asyncMapNotNull(workerCount = Runtime.getRuntime().availableProcessors(), block = map)
        }
        println("$name#asyncMapNotNull = $duration")
    }

    private suspend inline fun <T : Any, R : Any> runAsyncJobs(
        name: String,
        original: List<T>,
        expected: List<R>,
        noinline map: (T) -> R?,
    ) {
        val duration = runTest(REPEAT_COUNT, expected) {
            coroutineScope {
                original.map {
                    async { map(it) }
                }.awaitAll().filterNotNull()
            }
        }
        println("$name#asyncJobs = $duration")
    }

    private suspend inline fun <T : Any, R : Any> runParallelStreams(
        name: String,
        original: List<T>,
        expected: List<R>,
        noinline map: (T) -> R?,
    ) {
        val duration = runTest(REPEAT_COUNT, expected) {
            original.parallelStream().map(map).filter { it != null }.toList() as List<R>
        }
        println("$name#parallelStreams = $duration")
    }

    private suspend inline fun <R : Any> runTest(
        repeatCount: Int,
        expected: List<R>,
        block: suspend () -> List<R>,
    ): Duration {
        val times = mutableListOf<Duration>()
        repeat(repeatCount) {
            val result: List<R>
            val time = measureTime {
                result = block()
            }
            assertEquals(expected, result)
            times += time
        }
        return times.fold(0.seconds) { acc, it -> acc + it } / times.size
    }

    companion object Companion {
        private const val ELEMENT_COUNT: Int = 1_000
        private const val REPEAT_COUNT: Int = 1_000
    }
}