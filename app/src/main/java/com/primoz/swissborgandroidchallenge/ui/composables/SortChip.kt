package com.primoz.swissborgandroidchallenge.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.primoz.swissborgandroidchallenge.R
import com.primoz.swissborgandroidchallenge.helpers.FilterType
import com.primoz.swissborgandroidchallenge.ui.theme.SwissBorgAndroidChallengeTheme

@Composable
fun SortChip(
    modifier: Modifier = Modifier,
    filter: FilterType? = null,
    onChipPressed: () -> Unit = {},
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, Color.Black.copy(0.12f)),
        color = if (filter == null) Color.Transparent else MaterialTheme.colors.primary.copy(0.12f)
    ) {
        Row(
            modifier = Modifier
                .clickable { onChipPressed() }
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

@Preview(showBackground = true)
@Composable
fun PreviewChip() {
    SwissBorgAndroidChallengeTheme {
        SortChip()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewActiveChip() {
    SwissBorgAndroidChallengeTheme {
        SortChip(filter = FilterType.SORT_GAIN)
    }
}
