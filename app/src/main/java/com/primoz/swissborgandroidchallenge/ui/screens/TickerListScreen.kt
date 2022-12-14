package com.primoz.swissborgandroidchallenge.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
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
import com.primoz.swissborgandroidchallenge.network.Response
import com.primoz.swissborgandroidchallenge.network.data.Ticker
import com.primoz.swissborgandroidchallenge.ui.composables.*
import com.primoz.swissborgandroidchallenge.ui.theme.Green
import com.primoz.swissborgandroidchallenge.ui.theme.Red
import com.primoz.swissborgandroidchallenge.ui.theme.ShapesTopOnly
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun TickerListScreen(
    modifier: Modifier = Modifier,
    viewModel: TickerListViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val secondsFromLastUpdate by viewModel.secondsFromLastUpdate.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val tickerResponse by viewModel.tickerListState.collectAsState()
    val currentAppliedFilter by viewModel.currentAppliedFilter.collectAsState()
    val toApplyFilter by viewModel.toApplyFilter.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val refreshingState = rememberSwipeRefreshState(isRefreshing)
    val listState = rememberLazyListState()

    var expandedTickerSymbol by remember { mutableStateOf<String?>(null) }

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

    // Whenever bottomSheet hides, update bottomSheet UI, so it has current applied filters
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
                onCloseButtonPressed = closeSheet,
                onFilterPressed = {
                    viewModel.selectFilterOption(it)
                },
                onApplyButtonPressed = {
                    viewModel.applySelectedFilters()
                    closeSheet()
                },
                onClearAllButtonPressed = {
                    viewModel.clearFilters()
                    closeSheet()
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.background(MaterialTheme.colors.background),
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

                // Sort
                SortChip(
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                    filter = currentAppliedFilter,
                    onChipPressed = {
                        openSheet()
                    }
                )

                Spacer(modifier = Modifier.padding(4.dp))

                // Content
                TickerContent(
                    tickerResponse = tickerResponse,
                    secondsFromLastUpdate = secondsFromLastUpdate,
                    refreshingState = refreshingState,
                    expandedTickerSymbol = expandedTickerSymbol,
                    listState = listState,
                    searchQuery = searchQuery,
                    onTickerPressed = {
                        expandedTickerSymbol = if (expandedTickerSymbol == it?.symbol) null else it?.symbol
                    },
                    onRetryPressed = {
                        viewModel.getNewTickerList()
                    }
                ) {
                    viewModel.refreshTickers()
                }
            }
        }
    }
}

@Composable
private fun TickerContent(
    tickerResponse: Response<List<Ticker>> = Response.Loading,
    secondsFromLastUpdate: Int = 0,
    refreshingState: SwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false),
    expandedTickerSymbol: String? = null,
    listState: LazyListState = rememberLazyListState(),
    searchQuery: String = "",
    onTickerPressed: (Ticker?) -> Unit = {},
    onRetryPressed: () -> Unit = {},
    onRefresh: () -> Unit = {},
) {
    val context = LocalContext.current

    when (tickerResponse) {
        is Response.Loading -> {
            LoadingScreen()
        }
        is Response.Error -> {
            LaunchedEffect(tickerResponse.key) { // Key to trigger next time error shows
                Toast.makeText(
                    context,
                    context.getString(R.string.error_could_not_retrieve_coins),
                    Toast.LENGTH_SHORT
                ).show()
            }
            val items = tickerResponse.data
            if (items.isNullOrEmpty()) {
                ErrorScreen(onRetryPressed = onRetryPressed)
            } else {
                TickerList(
                    modifier = Modifier.fillMaxSize(),
                    items = items,
                    secondsFromLastUpdate = secondsFromLastUpdate,
                    enableRefreshing = true,
                    refreshingState = refreshingState,
                    expandedTickerSymbol = expandedTickerSymbol,
                    listState = listState,
                    searchQuery = searchQuery,
                    onTickerPressed = { onTickerPressed(it) },
                    onRefresh = onRefresh
                )
            }
        }
        is Response.Success -> {
            val items = tickerResponse.data
            if (items.isEmpty()) {
                ErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    message = stringResource(R.string.error_no_items, searchQuery),
                    showRetry = false
                )
            }
            TickerList(
                modifier = Modifier.fillMaxSize(),
                items = items,
                secondsFromLastUpdate = secondsFromLastUpdate,
                enableRefreshing = false,
                refreshingState = refreshingState,
                listState = listState,
                expandedTickerSymbol = expandedTickerSymbol,
                onTickerPressed = { onTickerPressed(it) },
                onRefresh = onRefresh,
                searchQuery = searchQuery
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TickerList(
    modifier: Modifier = Modifier,
    items: List<Ticker> = listOf(),
    secondsFromLastUpdate: Int = 0,
    enableRefreshing: Boolean = false,
    refreshingState: SwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false),
    listState: LazyListState = rememberLazyListState(),
    expandedTickerSymbol: String? = null,
    searchQuery: String = "",
    onTickerPressed: (Ticker?) -> Unit = {},
    onRefresh: () -> Unit = {},
) {
    // This is a hack for scroll position problem when using animateItemPlacement in LazyColumn bellow.
    // Without this the scroll will be focused on the first item (which is correct), but if another item goes above the first one (sort)
    // the focus won't move to the new one but remain on the old one and now the new first item is now hidden and user needs to scroll
    // to see it
    LaunchedEffect(items.firstOrNull()) {
        if (items.isNotEmpty() && searchQuery.isEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
    SwipeRefresh(
        modifier = modifier,
        state = refreshingState,
        swipeEnabled = enableRefreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp),
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
                    TickerItem(
                        modifier = Modifier
                            .clickable { onTickerPressed(ticker) }
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 16.dp),
                        expandedTickerSymbol = expandedTickerSymbol,
                        icon = ticker.icon,
                        name = ticker.name,
                        formattedLastPrice = ticker.formattedLastPrice,
                        formattedDailyChangePercentage = ticker.formattedDailyChangePercentage,
                        dailyChangePercentage = ticker.dailyChangePercentage,
                        symbol = ticker.symbol,
                        currentRatio = ticker.ratio,
                        formattedLow = ticker.formattedLow,
                        formattedHigh = ticker.formattedHigh,
                    )
                }
            }
            item(key = "footer") {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .animateContentSize(
                            animationSpec = tween(durationMillis = 200)
                        )
                        .animateItemPlacement(),
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
    expandedTickerSymbol: String? = null,
    icon: Int = R.drawable.ic_btc,
    name: String = "Bitcoin",
    formattedLastPrice: String = "$10.0",
    formattedDailyChangePercentage: String = "+10.0%",
    dailyChangePercentage: Float = 10.0f,
    symbol: String = "BTC",
    currentRatio: Float = 0.5f,
    formattedHigh: String = "20.0",
    formattedLow: String = "10.0",
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
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
            visible = expandedTickerSymbol != null && expandedTickerSymbol == symbol,
            enter = enterTransition,
            exit = exitTransition
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(48.dp)
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
                        color = if (currentRatio < 0.5f) Red else MaterialTheme.colors.primary,
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

