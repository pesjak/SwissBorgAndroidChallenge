package com.primoz.swissborgandroidchallenge

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.primoz.swissborgandroidchallenge.composables.FilterType
import com.primoz.swissborgandroidchallenge.composables.FiltersBottomSheet
import com.primoz.swissborgandroidchallenge.composables.SearchComposable
import com.primoz.swissborgandroidchallenge.network.Response
import com.primoz.swissborgandroidchallenge.network.data.Ticker
import com.primoz.swissborgandroidchallenge.ui.theme.Green
import com.primoz.swissborgandroidchallenge.ui.theme.Red
import com.primoz.swissborgandroidchallenge.ui.theme.ShapesTopOnly
import com.primoz.swissborgandroidchallenge.ui.theme.SwissBorgAndroidChallengeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwissBorgAndroidChallengeTheme {

                val coroutineScope = rememberCoroutineScope()
                val context = LocalContext.current
                val viewModel: TickerListViewModel = hiltViewModel()
                val searchQuery by viewModel.searchQuery
                val secondsFromLastUpdate by viewModel.secondsFromLastUpdate
                val tickerListState by viewModel.tickerListState.collectAsState()
                val isRefreshing by viewModel.isRefreshing.collectAsState()
                val refreshingState = rememberSwipeRefreshState(isRefreshing)
                val listState = rememberLazyListState()

                val currentAppliedFilter by viewModel.currentAppliedFilter.collectAsState()
                val toApplyFilter by viewModel.toApplyFilter.collectAsState()

                val bottomSheetState = rememberModalBottomSheetState(
                    initialValue = ModalBottomSheetValue.Hidden,
                    skipHalfExpanded = true
                )

                val closeSheet: () -> Unit = {
                    coroutineScope.launch {
                        bottomSheetState.hide()
                    }
                }

                val openSheet: () -> Unit = {
                    coroutineScope.launch {
                        bottomSheetState.show()
                    }
                }

                // Whenever bottomSheetState is changed, update UI to currently applied filters
                LaunchedEffect(bottomSheetState.currentValue) {
                    if (bottomSheetState.currentValue == ModalBottomSheetValue.Hidden) {
                        viewModel.selectFilterOption(currentAppliedFilter)
                    }
                }

                ModalBottomSheetLayout(
                    sheetState = bottomSheetState,
                    sheetElevation = 8.dp,
                    sheetShape = ShapesTopOnly.large,
                    sheetContent = {
                        FiltersBottomSheet(
                            filterSortType = currentAppliedFilter,
                            toApplyFilter = toApplyFilter,
                            bottomSheetState = bottomSheetState,
                            sortPressed = {
                                viewModel.selectFilterOption(it)
                            },
                            closeButtonPressed = {
                                closeSheet()
                            },
                            applyButtonClick = {
                                viewModel.applySelectedFilters()
                                closeSheet()
                            },
                            clearAllButtonClick = {
                                viewModel.clearFilters()
                                closeSheet()
                            }
                        )
                    }
                ) {
                    Scaffold(
                        modifier = Modifier
                            .background(MaterialTheme.colors.background),
                    ) { contentPadding ->
                        Column(
                            modifier = Modifier
                                .padding(contentPadding)
                        ) {
                            // Search
                            SearchComposable(
                                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                                text = searchQuery,
                                onTextChange = {
                                    viewModel.updateSearchQuery(it)
                                },
                            )

                            FilterChip(
                                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                                filter = currentAppliedFilter,
                                chipPressed = {
                                    openSheet()
                                }
                            )

                            Spacer(modifier = Modifier.padding(4.dp))

                            when (val state = tickerListState) {
                                is Response.Loading -> {
                                    LoadingScreen(
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                else -> {
                                    val items = if (state is Response.Success) state.data else (state as Response.Error).data

                                    if (state is Response.Error) {
                                        LaunchedEffect(state.key) { // Key to trigger next time error shows
                                            Toast.makeText(
                                                context,
                                                getString(R.string.error_could_not_retrieve_coins),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    // Ticker List
                                    if (items == null) {
                                        ErrorScreen(
                                            modifier = Modifier.fillMaxSize(),
                                            onRetryPressed = {
                                                viewModel.loadTickers()
                                            },
                                        )
                                    } else {
                                        if (items.isEmpty()) {
                                            ErrorScreen(
                                                modifier = Modifier.fillMaxSize(),
                                                message = getString(R.string.error_no_items, searchQuery),
                                                showRetry = false
                                            )
                                        }
                                        TickerList(
                                            items = items,
                                            secondsFromLastUpdate = secondsFromLastUpdate,
                                            enableRefreshing = state is Response.Error,
                                            refreshingState = refreshingState,
                                            listState = listState,
                                            onRefreshItems = {
                                                viewModel.refreshTickers()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LoadingScreen(
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }

    @Composable
    fun ErrorScreen(
        modifier: Modifier = Modifier,
        message: String = stringResource(R.string.error_something_went_wrong),
        buttonText: String = stringResource(R.string.error_try_again),
        showRetry: Boolean = true,
        onRetryPressed: () -> Unit = {},
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h6,
            )
            if (showRetry) {
                OutlinedButton(onClick = onRetryPressed) {
                    Text(text = buttonText)
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        SwissBorgAndroidChallengeTheme {

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TickerList(
    items: List<Ticker> = listOf(),
    secondsFromLastUpdate: Int = 0,
    enableRefreshing: Boolean,
    onRefreshItems: () -> Unit = {},
    refreshingState: SwipeRefreshState,
    listState: LazyListState,
) {

    SwipeRefresh(
        state = refreshingState,
        swipeEnabled = enableRefreshing,
        onRefresh = {
            onRefreshItems()
        }
    ) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
        ) {
            items(items, key = { it.symbol }) { ticker ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            border = BorderStroke(1.dp, Color.Black.copy(0.12f)),
                            shape = MaterialTheme.shapes.medium
                        )
                        .animateItemPlacement(),
                    elevation = 0.dp,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { // Clickable inside row and not card because ripple isn't clipped and we don't want "Experimental"
                                // TODO maybe expand/collapse
                            }
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = ticker.icon),
                            contentDescription = "Icon",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(32.dp)
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                modifier = Modifier,
                                text = ticker.name,
                                style = MaterialTheme.typography.subtitle1
                            )

                            Text(
                                modifier = Modifier,
                                text = ticker.symbol,
                                style = MaterialTheme.typography.caption
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = ticker.formattedLastPrice,
                                style = MaterialTheme.typography.subtitle1
                            )
                            Text(
                                style = MaterialTheme.typography.subtitle2,
                                text = ticker.formattedDailyChangePercentage,
                                color = if (ticker.dailyChangePercentage > 0) Green else Red
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .animateContentSize(
                            animationSpec = tween(durationMillis = 200)
                        ),
                    text = if (secondsFromLastUpdate < 2) {
                        stringResource(id = R.string.last_updated_now)
                    } else {
                        stringResource(R.string.last_updated_seconds, secondsFromLastUpdate)
                    },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    modifier: Modifier = Modifier,
    filter: FilterType? = null,
    chipPressed: () -> Unit = {},
) {
    Surface(
        modifier = modifier.padding(end = 4.dp),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, Color.Black.copy(0.12f)),
        color = if (filter == null) Color.Transparent else MaterialTheme.colors.primary.copy(0.12f)
    ) {
        Row(
            modifier = Modifier
                .clickable { chipPressed() }
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.FilterList,
                contentDescription = "filter",
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 8.dp, start = 8.dp)
                    .size(20.dp)
            )
            Text(
                text = stringResource(id = filter?.stringResource ?: R.string.sort_none),
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
