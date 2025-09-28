package ru.vladislavsumin.core.coroutines.collectionUtils

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlin.math.min

public suspend fun <T, R> List<T>.asyncTransform(
    bucketSize: Int = 32, // TODO вычислять динамически
    workerCount: Int = 4, // TODO смотреть на limitedParallelism
    block: suspend (T) -> R?,
): List<R> {
    return AsyncListTransformer(this, bucketSize, workerCount, block).transform()
}

private class AsyncListTransformer<T, R>(
    private val original: List<T>,
    private val bucketSize: Int,
    private val workerCount: Int,
    private val block: suspend (T) -> R?,
) {
    // Вся байда с классом нужна из-за atomic. Он не умеет создаваться в функции и может использоваться только
    // в создавшем его классе.
    private val firstUnprocessedBucket = atomic(0)

    suspend fun transform(): List<R> = coroutineScope {

        // Мы явно указали результату максимально возможный размер, что бы избежать изменения размера листа во время
        // выполнения. Таким образом мы можем спокойно писать в этот лист асинхронно.
        val result = ArrayList<R?>(original.size)
        repeat(original.size) { result.add(null) }

        val lastBucket = original.size / bucketSize

        val buckedEndIndexes = IntArray(lastBucket + 1)

        fun runWorker(): Job = async {
            while (true) {
                val currentBucket = firstUnprocessedBucket.getAndIncrement()
                if (currentBucket > lastBucket) break
                var originalIndex = currentBucket * bucketSize
                var resultIndex = originalIndex
                val lastIndex = min(originalIndex + bucketSize, original.size)
                while (originalIndex < lastIndex) {
                    val blockResult = block(original[originalIndex])
                    if (blockResult != null) {
                        result[resultIndex] = blockResult
                        resultIndex++
                    }
                    originalIndex++
                }
                buckedEndIndexes[currentBucket] = resultIndex
            }
        }

        val workersJob = (0 until workerCount).map { runWorker() }
        workersJob.joinAll()

        var resultIndex = 0
        buckedEndIndexes.forEachIndexed { bucketIndex, lastIndex ->
            val startIndex = bucketIndex * bucketSize
            for (index in (startIndex until lastIndex)) {
                result[resultIndex++] = result[index]
            }
        }
        repeat(result.size - resultIndex) {
            result.removeLast()
        }

        result.trimToSize()
        result as List<R>
    }
}
