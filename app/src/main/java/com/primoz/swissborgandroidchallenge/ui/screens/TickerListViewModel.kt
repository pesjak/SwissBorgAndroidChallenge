package com.primoz.swissborgandroidchallenge.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primoz.swissborgandroidchallenge.helpers.FilterType
import com.primoz.swissborgandroidchallenge.network.BitFinexClient
import com.primoz.swissborgandroidchallenge.network.Response
import com.primoz.swissborgandroidchallenge.network.data.Ticker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TickerListViewModel @Inject constructor(
    private val client: BitFinexClient
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery get() = _searchQuery

    private val _secondsFromLastUpdate = MutableStateFlow(0)
    val secondsFromLastUpdate get() = _secondsFromLastUpdate

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing get() = _isRefreshing

    private val _currentAppliedFilter = MutableStateFlow<FilterType?>(null)
    val currentAppliedFilter get() = _currentAppliedFilter

    private val _toApplyFilter = MutableStateFlow<FilterType?>(null)
    val toApplyFilter get() = _toApplyFilter

    private val _tickerListState = MutableStateFlow<Response<List<Ticker>>>(Response.Loading)
    val tickerListState get() = _tickerListState

    private var shouldUpdateTickers = true

    private var unFilteredList = mutableListOf<Ticker>()

    init {
        getNewTickerList()
        updateTickersEveryFewSeconds()
    }

    fun refreshTickers() {
        _isRefreshing.value = true
        getTickerList()
    }

    fun getNewTickerList() {
        _tickerListState.value = Response.Loading
        getTickerList()
    }

    fun updateSearchQuery(searchQuery: String) {
        _searchQuery.value = searchQuery
        val newTickerList = filterTickerList()
        updateTickerList(newTickerList)
    }

    fun selectFilterOption(filterOption: FilterType?) {
        _toApplyFilter.value = filterOption
    }

    fun applySelectedFilters() {
        _currentAppliedFilter.value = _toApplyFilter.value
        _toApplyFilter.value = null

        val newTickerList = filterTickerList()
        updateTickerList(newTickerList)
    }

    fun clearFilters() {
        _currentAppliedFilter.value = null
        _toApplyFilter.value = null
        val newTickerList = filterTickerList()
        updateTickerList(newTickerList)
    }

    private fun getTickerList() {
        viewModelScope.launch(Dispatchers.IO) {
            client.getTickers()
                .onSuccess {
                    _secondsFromLastUpdate.value = 0
                    shouldUpdateTickers = true
                    _isRefreshing.value = false
                    unFilteredList = it.toMutableList()

                    val filteredList = filterTickerList()
                    _tickerListState.value = Response.Success(filteredList)
                }
                .onFailure {
                    shouldUpdateTickers = false
                    val currentState = tickerListState.value
                    _tickerListState.value = Response.Error( // Don't update the items
                        key = if (isRefreshing.value) "refresh$secondsFromLastUpdate" else "get",
                        data = (currentState as? Response.Success)?.data ?: (currentState as? Response.Error)?.data,
                        message = it.message ?: "Something went wrong"
                    )
                    _isRefreshing.value = false
                }
        }
    }

    private fun updateTickerList(newTickerList: List<Ticker>) {
        when (_tickerListState.value) {
            is Response.Error -> _tickerListState.value = Response.Error(key = "update", data = newTickerList, message = "No Data")
            Response.Loading -> {} // No need to handle this
            is Response.Success -> _tickerListState.value = Response.Success(data = newTickerList)
        }
    }

    private fun updateTickersEveryFewSeconds() {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                while (true) {
                    if (shouldUpdateTickers) {
                        delay(5000L)
                        getTickerList()
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

    private fun filterTickerList(): List<Ticker> {
        val filteredList = searchTickers(unFilteredList)
        return when (currentAppliedFilter.value) {
            FilterType.SORT_GAIN -> filteredList.sortedByDescending { it.dailyChangePercentage }
            FilterType.SORT_LOSS -> filteredList.sortedBy { it.dailyChangePercentage }
            else -> filteredList.sortedBy { it.name }
        }
    }

    private fun searchTickers(tickers: List<Ticker>): List<Ticker> {
        return tickers.filter {
            it.name.lowercase().contains(searchQuery.value.lowercase()) || it.symbol.lowercase().contains(searchQuery.value.lowercase())
        }
    }
}
