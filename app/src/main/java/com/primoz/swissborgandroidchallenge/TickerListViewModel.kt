package com.primoz.swissborgandroidchallenge

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primoz.swissborgandroidchallenge.composables.FilterType
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

    fun refreshTickers() {
        loadTickers(shouldResetState = false, shouldRefresh = true)
    }

    fun loadTickers(
        shouldResetState: Boolean = true,
        shouldRefresh: Boolean = false,
    ) {
        if (shouldResetState) {
            _tickerListState.value = Response.Loading
        }

        if (shouldRefresh) {
            _isRefreshing.value = true
        }

        viewModelScope.launch {
            client.getTickers(searchQuery.value)
                .onSuccess {
                    _secondsFromLastUpdate.value = 0
                    shouldUpdateTickers = true
                    _isRefreshing.value = false

                    unFilteredList = it.toMutableList()

                    var filteredList = searchTickers(unFilteredList)
                    if (currentAppliedFilter.value == FilterType.SORT_GAIN) {
                        filteredList = filteredList.sortedByDescending { it.dailyChangePercentage }
                    } else if (currentAppliedFilter.value == FilterType.SORT_LOSS) {
                        filteredList = filteredList.sortedBy { it.dailyChangePercentage }
                    }

                    _tickerListState.value = Response.Success(filteredList)
                }
                .onFailure {
                    shouldUpdateTickers = false
                    _isRefreshing.value = false
                    val currentState = tickerListState.value
                    _tickerListState.value = Response.Error(
                        data = (currentState as? Response.Success)?.data ?: (currentState as? Response.Error)?.data,
                        message = it.message ?: "Something went wrong"
                    )
                }
        }
    }

    fun updateSearchQuery(searchQuery: String) {
        _searchQuery.value = searchQuery
        var filteredData = searchTickers(unFilteredList)
        if (currentAppliedFilter.value == FilterType.SORT_GAIN) {
            filteredData = filteredData.sortedByDescending { it.dailyChangePercentage }
        } else if (currentAppliedFilter.value == FilterType.SORT_LOSS) {
            filteredData = filteredData.sortedBy { it.dailyChangePercentage }
        }
        when (_tickerListState.value) {
            is Response.Error -> _tickerListState.value = Response.Error(key = "search", data = filteredData, message = "No Data")
            Response.Loading -> {} // No need to handle this
            is Response.Success -> _tickerListState.value = Response.Success(data = filteredData)
        }
    }

    fun filtersApplied(): FilterType? {
        if (currentAppliedFilter.value == FilterType.SORT_GAIN) {
            return FilterType.SORT_GAIN
        } else if (currentAppliedFilter.value == FilterType.SORT_LOSS) {
            return FilterType.SORT_LOSS
        }
        return null
    }

    fun selectFilterOption(filterOption: FilterType?) {
        _toApplyFilter.value = filterOption
    }

    fun applySelectedFilters() {
        var filteredData = searchTickers(unFilteredList)
        _currentAppliedFilter.value = _toApplyFilter.value
        _toApplyFilter.value = null

        if (currentAppliedFilter.value == FilterType.SORT_GAIN) {
            filteredData = filteredData.sortedByDescending { it.dailyChangePercentage }
        } else if (currentAppliedFilter.value == FilterType.SORT_LOSS) {
            filteredData = filteredData.sortedBy { it.dailyChangePercentage }
        }
        when (_tickerListState.value) {
            is Response.Error -> _tickerListState.value = Response.Error(key = "search", data = filteredData, message = "No Data")
            Response.Loading -> {} // No need to handle this
            is Response.Success -> _tickerListState.value = Response.Success(data = filteredData)
        }
    }

    fun clearFilters() {
        _currentAppliedFilter.value = null
    }

    private fun searchTickers(tickers: List<Ticker>): List<Ticker> {
        return tickers.filter {
            it.name.lowercase().contains(searchQuery.value.lowercase()) || it.symbol.lowercase().contains(searchQuery.value.lowercase())
        }
    }

}
