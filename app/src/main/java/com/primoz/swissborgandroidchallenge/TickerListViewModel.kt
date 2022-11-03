package com.primoz.swissborgandroidchallenge

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primoz.swissborgandroidchallenge.network.BitFinexClient
import com.primoz.swissborgandroidchallenge.network.Response
import com.primoz.swissborgandroidchallenge.network.data.Ticker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TickerListViewModel @Inject constructor(
    private val client: BitFinexClient
) : ViewModel() {

    private val _searchQuery = mutableStateOf("")
    val searchQuery get() = _searchQuery

    private val _secondsFromLastUpdate = mutableStateOf(0)
    val secondsFromLastUpdate get() = _secondsFromLastUpdate

    private val _tickerListState = MutableStateFlow<Response<List<Ticker>>>(Response.Loading)
    val tickerListState get() = _tickerListState

    private var shouldUpdateTickers = true

    init {
        loadTickers()
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                while (true) {
                    if (shouldUpdateTickers) {
                        loadTickers(shouldResetState = false)
                        delay(5000L)
                    }
                }
            }
            launch(Dispatchers.IO) {
                while (true) {
                    delay(1000L)
                    _secondsFromLastUpdate.value += 1
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        shouldUpdateTickers = false
    }

    fun loadTickers(
        shouldResetState: Boolean = true,
    ) {
        if (shouldResetState) _tickerListState.value = Response.Loading

        viewModelScope.launch {
            client.getTickers(searchQuery.value)
                .onSuccess {
                    _secondsFromLastUpdate.value = 0
                    shouldUpdateTickers = true
                    _tickerListState.value = Response.Success(it)
                }
                .onFailure {
                    shouldUpdateTickers = false
                    val currentState = tickerListState.value
                    _tickerListState.value = Response.Error(
                        data = (currentState as? Response.Success)?.data,
                        message = it.message ?: "Something went wrong"
                    )
                }
        }
    }

    fun updateSearchQuery(searchQuery: String) {
        _searchQuery.value = searchQuery
    }
}
