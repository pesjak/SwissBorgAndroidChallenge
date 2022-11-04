package com.primoz.swissborgandroidchallenge.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.primoz.swissborgandroidchallenge.R
import com.primoz.swissborgandroidchallenge.ui.theme.Shapes

enum class FilterType(val stringResource: Int) {
    SORT_GAIN(R.string.sort_gain),
    SORT_LOSS(R.string.sort_loss)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FiltersBottomSheet(
    modifier: Modifier = Modifier,
    filterSortType: FilterType? = null,
    sortPressed: (FilterType?) -> Unit = {},
    closeButtonPressed: () -> Unit = {},
    clearAllButtonClick: () -> Unit = {},
    applyButtonClick: () -> Unit = {},
    bottomSheetState: ModalBottomSheetState,
    toApplyFilter: FilterType?,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {

        Column {
            HandleBarFilters(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 0.dp, top = 16.dp),
                text = stringResource(R.string.filters_title),
                closeButtonClicked = closeButtonPressed
            )
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            ) {
                RadioButtonItem(
                    filterOption = FilterType.SORT_GAIN,
                    selectedItem = toApplyFilter,
                    radioButtonPressed = {
                        sortPressed(it)
                    }
                )
                RadioButtonItem(
                    filterOption = FilterType.SORT_LOSS,
                    selectedItem = toApplyFilter,
                    radioButtonPressed = {
                        sortPressed(it)
                    }
                )
            }
            BottomSheetMainButtons(
                applyButtonClick = {
                    applyButtonClick()
                },
                clearAllButtonClick = {
                    clearAllButtonClick()
                }
            )
        }
    }
}

@Composable
fun HandleBarFilters(
    modifier: Modifier = Modifier,
    text: String = "Title",
    closeButtonClicked: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp, 4.dp)
                .clip(Shapes.large)
                .background(Color.LightGray)
        )
    }

    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (close, title) = createRefs()
        IconButton(
            modifier = Modifier.constrainAs(close) {
                start.linkTo(parent.start)
            },
            onClick = closeButtonClicked
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close filters",
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
            )
        }
        Text(
            modifier = Modifier.constrainAs(title) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
            style = MaterialTheme.typography.subtitle1,
            text = text
        )
    }
}

@Composable
private fun BottomSheetMainButtons(
    modifier: Modifier = Modifier,
    clearAllButtonClick: () -> Unit = {},
    applyButtonClick: () -> Unit = {},
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        val (clearButton, applyButton) = createRefs()
        OutlinedButton(
            modifier = Modifier
                .constrainAs(clearButton) {
                    start.linkTo(parent.start)
                    end.linkTo(applyButton.end)
                    width = Dimension.fillToConstraints
                }
                .padding(start = 8.dp, end = 16.dp),
            onClick = clearAllButtonClick
        ) {
            Text(
                text = stringResource(R.string.filters_clear_all).uppercase(),
                color = MaterialTheme.colors.onBackground
            )
        }
        Button(
            modifier = Modifier
                .constrainAs(applyButton) {
                    start.linkTo(clearButton.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
                .padding(start = 16.dp),
            onClick = applyButtonClick
        ) {
            Text(stringResource(R.string.filters_apply).uppercase())
        }
        createHorizontalChain(
            clearButton,
            applyButton,
            chainStyle = ChainStyle.Spread
        )
    }
}

@Composable
fun RadioButtonItem(
    filterOption: FilterType = FilterType.SORT_GAIN,
    selectedItem: FilterType? = null,
    radioButtonPressed: (FilterType?) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = filterOption == selectedItem,
                onClick = { radioButtonPressed(filterOption) },
                role = Role.RadioButton
            )
            .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = stringResource(id = filterOption.stringResource),
        )
        Spacer(Modifier.weight(1f))
        RadioButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            selected = filterOption == selectedItem,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colors.primary
            ),
            onClick = null
        )
    }
}
