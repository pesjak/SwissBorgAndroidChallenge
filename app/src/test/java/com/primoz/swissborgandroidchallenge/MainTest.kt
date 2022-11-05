package com.primoz.swissborgandroidchallenge

import com.primoz.swissborgandroidchallenge.TestTickersApi.Companion.exampleList
import com.primoz.swissborgandroidchallenge.network.TickersRequests
import com.primoz.swissborgandroidchallenge.network.data.Ticker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainTest {
    private val testCoroutineDispatcher = StandardTestDispatcher()
    private val testCoroutineScope = TestScope(testCoroutineDispatcher)

    private lateinit var clientRequests: TickersRequests

    @Before
    fun setup() {
        Dispatchers.setMain(testCoroutineDispatcher)
        clientRequests = TestTickersApi()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun client_getTickerList() = testCoroutineScope.runTest {
        val request = clientRequests.getTickers()
        var list = listOf<Ticker>()
        request.onSuccess {
            list = it
        }
        advanceTimeBy(1000)

        assertEquals(exampleList, list)
    }
}
