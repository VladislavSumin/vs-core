package ru.vladislavsumin.core.coroutines.collectionUtils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals

class ListAsyncMapNotNullTest {
    @Test
    fun mapCollectionWithoutNulls() = runBlocking {
        val original = buildList<Int>(ELEMENT_COUNT) {
            repeat(ELEMENT_COUNT) {
                add(it)
            }
        }
        withContext(Dispatchers.Default) {
            val transformed = original.asyncMapNotNull { -it }
            val expected = original.map { -it }
            assertEquals(expected, transformed)

        }
    }

    @Test
    fun mapCollectionWitNulls() = runBlocking {
        val original = buildList<Int>(ELEMENT_COUNT) {
            repeat(ELEMENT_COUNT) {
                add(it)
            }
        }
        withContext(Dispatchers.Default) {
            val transformed = original.asyncMapNotNull { if (it % 2 == 0) it else null }
            val expected = original.mapNotNull { if (it % 2 == 0) it else null }
            assertEquals(expected, transformed)

        }
    }


    companion object Companion {
        private const val ELEMENT_COUNT: Int = 1_000_000
    }
}