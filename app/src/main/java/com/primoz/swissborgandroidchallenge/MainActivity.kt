package com.primoz.swissborgandroidchallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.primoz.swissborgandroidchallenge.composables.SearchComposable
import com.primoz.swissborgandroidchallenge.network.Response
import com.primoz.swissborgandroidchallenge.ui.theme.Purple700
import com.primoz.swissborgandroidchallenge.ui.theme.SwissBorgAndroidChallengeTheme
import com.primoz.swissborgandroidchallenge.ui.theme.Teal700
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwissBorgAndroidChallengeTheme {

                val viewModel: TickerListViewModel = hiltViewModel()
                val searchQuery by viewModel.searchQuery
                val tickerListState by viewModel.tickerListState.collectAsState()

                Scaffold(
                    modifier = Modifier
                        .background(MaterialTheme.colors.background)
                        .padding(8.dp),
                ) { contentPadding ->
                    Column(
                        modifier = Modifier
                            .padding(contentPadding)
                            .padding(horizontal = 8.dp)
                    ) {
                        // Search
                        SearchComposable(
                            text = searchQuery,
                            onTextChange = {
                                //  viewModel.updateSearchQuery(it)
                            },
                        )

                        when (val state = tickerListState) {
                            is Response.Error -> {
                                ErrorScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    onRetryPressed = {
                                        viewModel.searchTickers()
                                    }
                                )
                            }
                            Response.Loading -> {
                                LoadingScreen(
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            is Response.Success -> {
                                // Ticker List
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(bottom = 16.dp)
                                ) {
                                    itemsIndexed(state.data) { index, ticker ->
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
                                                        text = String.format("$%.4f", ticker.lastPrice),
                                                        style = MaterialTheme.typography.subtitle1
                                                    )
                                                    Text(
                                                        style = MaterialTheme.typography.subtitle2,
                                                        text = String.format("%.2f%%", ticker.dailyChangeRelative),
                                                        color = if (ticker.dailyChangeRelative > 0) Teal700 else Purple700
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
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
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
    onRetryPressed: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.h6,
        )
        OutlinedButton(onClick = onRetryPressed) {
            Text(text = buttonText)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SwissBorgAndroidChallengeTheme {
        Greeting("Android")
    }
}
