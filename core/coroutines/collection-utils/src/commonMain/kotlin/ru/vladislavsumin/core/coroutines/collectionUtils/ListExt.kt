package ru.vladislavsumin.core.coroutines.collectionUtils

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlin.math.min

public suspend fun <T : Any, R : Any> List<T>.asyncMapNotNull(
    workerCount: Int = 4, // Мы не можем узнать current limited parallelism, тут нам нужна подсказка.
    bucketSize: Int = min(32, size / workerCount + 1),
    block: suspend (T) -> R?,
): List<R> = AsyncListTransformer(original = this, workerCount, bucketSize, block).transform()

public class AsyncListTransformer<T : Any, R : Any>(
    private val original: List<T>,
    private val workerCount: Int,
    private val bucketSize: Int,
    private val block: suspend (T) -> R?,
) {
    // Вся байда с классом нужна из-за atomic. Он не умеет создаваться в функции и может использоваться только
    // в создавшем его классе.
    private val firstUnprocessedBucket = atomic(0)

    public suspend fun transform(): List<R> = coroutineScope {
        val result = arrayOfNulls<Any>(original.size)

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
                if (!coroutineContext.isActive) break
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

        result.slice(0 until resultIndex) as List<R>
    }
}
