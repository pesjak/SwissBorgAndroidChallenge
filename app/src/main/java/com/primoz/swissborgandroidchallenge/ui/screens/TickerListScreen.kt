package com.primoz.swissborgandroidchallenge.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.primoz.swissborgandroidchallenge.R
import com.primoz.swissborgandroidchallenge.helpers.FilterType
import com.primoz.swissborgandroidchallenge.network.Response
import com.primoz.swissborgandroidchallenge.network.data.Ticker
import com.primoz.swissborgandroidchallenge.ui.composables.ErrorScreen
import com.primoz.swissborgandroidchallenge.ui.composables.FiltersBottomSheet
import com.primoz.swissborgandroidchallenge.ui.composables.LoadingScreen
import com.primoz.swissborgandroidchallenge.ui.composables.SearchComposable
import com.primoz.swissborgandroidchallenge.ui.theme.Green
import com.primoz.swissborgandroidchallenge.ui.theme.Red
import com.primoz.swissborgandroidchallenge.ui.theme.ShapesTopOnly
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun TickerListScreen() {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val viewModel: TickerListViewModel = hiltViewModel()
    val searchQuery by viewModel.searchQuery
    val secondsFromLastUpdate by viewModel.secondsFromLastUpdate

    val tickerListState by viewModel.tickerListState.collectAsState()
    val currentAppliedFilter by viewModel.currentAppliedFilter.collectAsState()
    val toApplyFilter by viewModel.toApplyFilter.collectAsState()

    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val refreshingState = rememberSwipeRefreshState(isRefreshing)
    val listState = rememberLazyListState()

    var expandedTicker by remember { mutableStateOf<Ticker?>(null) }

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
                toApplyFilter = toApplyFilter,
                filterPressed = {
                    viewModel.selectFilterOption(it)
                },
                closeButtonPressed = {
                    closeSheet()
                },
                applyButtonPressed = {
                    viewModel.applySelectedFilters()
                    closeSheet()
                },
                clearAllButtonPressed = {
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

                SortChip(
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                    filter = currentAppliedFilter,
                    chipPressed = {
                        openSheet()
                    }
                )

                Spacer(modifier = Modifier.padding(4.dp))

                when (val state = tickerListState) {
                    is Response.Loading -> LoadingScreen()
                    is Response.Error -> {
                        LaunchedEffect(state.key) { // Key to trigger next time error shows
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_could_not_retrieve_coins),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        val items = state.data
                        if (items == null) {
                            ErrorScreen(
                                onRetryPressed = {
                                    viewModel.getNewTickerList()
                                },
                            )
                        } else {
                            TickerContent(
                                modifier = Modifier.fillMaxSize(),
                                items = items,
                                secondsFromLastUpdate = secondsFromLastUpdate,
                                enableRefreshing = true,
                                refreshingState = refreshingState,
                                expandedTicker = expandedTicker,
                                listState = listState,
                                tickerPressed = {
                                    expandedTicker = if (expandedTicker == it) null else it
                                },
                                onRefreshItems = {
                                    viewModel.refreshTickers()
                                }
                            )
                        }
                    }
                    is Response.Success -> {
                        val items = state.data
                        // Ticker List
                        if (items.isEmpty()) {
                            ErrorScreen(
                                modifier = Modifier.fillMaxSize(),
                                message = stringResource(R.string.error_no_items, searchQuery),
                                showRetry = false
                            )
                        }
                        TickerContent(
                            modifier = Modifier.fillMaxSize(),
                            items = items,
                            secondsFromLastUpdate = secondsFromLastUpdate,
                            enableRefreshing = false,
                            refreshingState = refreshingState,
                            expandedTicker = expandedTicker,
                            listState = listState,
                            tickerPressed = {
                                expandedTicker = if (expandedTicker == it) null else it
                            },
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

@Composable
fun TickerContent(
    modifier: Modifier = Modifier,
    items: List<Ticker> = listOf(),
    secondsFromLastUpdate: Int = 0,
    enableRefreshing: Boolean,
    refreshingState: SwipeRefreshState,
    listState: LazyListState,
    expandedTicker: Ticker? = null,
    tickerPressed: (Ticker?) -> Unit = {},
    onRefreshItems: () -> Unit = {},
) {
    SwipeRefresh(
        modifier = modifier,
        state = refreshingState,
        swipeEnabled = enableRefreshing,
        onRefresh = {
            onRefreshItems()
        }
    ) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp),
        ) {
            items(items.size) { index ->
                val ticker = items[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            border = BorderStroke(1.dp, Color.Black.copy(0.12f)),
                            shape = MaterialTheme.shapes.medium
                        ),
                    elevation = 0.dp,
                    shape = MaterialTheme.shapes.medium
                ) {
                    TickerItem(
                        expandedTicker = expandedTicker,
                        icon = ticker.icon,
                        name = ticker.name,
                        formattedLastPrice = ticker.formattedLastPrice,
                        formattedDailyChangePercentage = ticker.formattedDailyChangePercentage,
                        dailyChangePercentage = ticker.dailyChangePercentage,
                        symbol = ticker.symbol,
                        currentRatio = ticker.ratio,
                        formattedLow = ticker.formattedLow,
                        formattedHigh = ticker.formattedHigh,
                        tickerPressed = {
                            tickerPressed(ticker)
                        }
                    )
                }
            }
            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .animateContentSize(
                            animationSpec = tween(durationMillis = 200)
                        )
                        .background(Color.Transparent),
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
private fun TickerItem(
    modifier: Modifier = Modifier,
    expandedTicker: Ticker? = null,
    icon: Int = R.drawable.ic_btc,
    name: String = "Bitcoin",
    formattedLastPrice: String = "$10.0",
    formattedDailyChangePercentage: String = "+10.0%",
    dailyChangePercentage: Float = 10.0f,
    symbol: String = "BTC",
    tickerPressed: () -> Unit = {},
    currentRatio: Float,
    formattedHigh: String,
    formattedLow: String,
) {
    Column {
        Row(
            modifier = modifier
                .clickable { // Clickable inside row and not card because ripple isn't clipped and we don't want "Experimental"
                    tickerPressed()
                }
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "Icon",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(32.dp)
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    modifier = Modifier,
                    text = name,
                    style = MaterialTheme.typography.subtitle1
                )

                Text(
                    modifier = Modifier,
                    text = symbol,
                    style = MaterialTheme.typography.caption
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formattedLastPrice,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    style = MaterialTheme.typography.subtitle2,
                    text = formattedDailyChangePercentage,
                    color = if (dailyChangePercentage > 0) Green else Red
                )
            }
        }

        val enterTransition = remember {
            expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = tween(250)
            )
        }
        val exitTransition = remember {
            shrinkVertically(
                // Expand from the top.
                shrinkTowards = Alignment.Top,
                animationSpec = tween(250)
            )
        }
        AnimatedVisibility(
            visible = expandedTicker != null && expandedTicker.name == name,
            enter = enterTransition,
            exit = exitTransition
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 9.dp)
                    .height(64.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Low")
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = formattedLow,
                        style = MaterialTheme.typography.subtitle2
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .height(8.dp)
                            .fillMaxWidth()
                            .clip(CircleShape),
                        progress = currentRatio,
                        backgroundColor = MaterialTheme.colors.onBackground.copy(0.12f)
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = "Today",
                        style = MaterialTheme.typography.subtitle2
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "High")
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = formattedHigh,
                        style = MaterialTheme.typography.subtitle2,
                    )
                }
            }
        }
    }
}

@Composable
fun SortChip(
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
