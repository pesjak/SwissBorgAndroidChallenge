package com.primoz.swissborgandroidchallenge

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primoz.swissborgandroidchallenge.network.BitFinexClient
import com.primoz.swissborgandroidchallenge.network.Response
import com.primoz.swissborgandroidchallenge.network.data.Ticker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TickerListViewModel @Inject constructor(
    private val client: BitFinexClient
) : ViewModel() {

    private val _searchQuery = mutableStateOf("")
    val searchQuery get() = _searchQuery

    private val _tickerListState = MutableStateFlow<Response<List<Ticker>>>(Response.Loading)
    val tickerListState get() = _tickerListState

    init {
        searchTickers()
    }

    fun searchTickers(
        searchQuery: String = ""
    ) {
        _tickerListState.value = Response.Loading
        viewModelScope.launch {
            client.getTickers(searchQuery)
                .onSuccess { _tickerListState.value = Response.Success(it) }
                .onFailure {
                    val currentState = tickerListState.value
                    _tickerListState.value = Response.Error(
                        data = (currentState as? Response.Success)?.data,
                        message = it.message ?: "Something went wrong"
                    )
                }
        }
    }
}
