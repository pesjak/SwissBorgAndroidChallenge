package com.primoz.swissborgandroidchallenge.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.primoz.swissborgandroidchallenge.ui.screens.TickerListScreen
import com.primoz.swissborgandroidchallenge.ui.theme.SwissBorgAndroidChallengeTheme
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalMaterialApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwissBorgAndroidChallengeTheme {
                TickerListScreen()
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        SwissBorgAndroidChallengeTheme {
            TickerListScreen()
        }
    }
}
