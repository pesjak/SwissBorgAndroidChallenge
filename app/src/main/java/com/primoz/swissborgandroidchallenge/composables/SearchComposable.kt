package com.primoz.swissborgandroidchallenge.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.primoz.swissborgandroidchallenge.R
@Composable
fun SearchComposable(
    modifier: Modifier = Modifier,
    text: String = "",
    onTextChange: (String) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = text,
            onValueChange = { onTextChange(it) },
            textStyle = MaterialTheme.typography.body1,
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                )
            },
            placeholder = {
                Text(
                    modifier = Modifier.alpha(0.5f),
                    style = MaterialTheme.typography.body1,
                    text = stringResource(id = R.string.search_item_hint)
                )
            },
            trailingIcon = {
                if (text != "") {
                    IconButton(
                        onClick = {
                            onTextChange("") // Remove text from TextField when you press the 'Close' icon
                        }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "",
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp)
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search,
                capitalization = KeyboardCapitalization.Sentences
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                }
            ),
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colors.onBackground,
                cursorColor = MaterialTheme.colors.onBackground,
                leadingIconColor = MaterialTheme.colors.onBackground,
                trailingIconColor = MaterialTheme.colors.onBackground,
                backgroundColor = MaterialTheme.colors.background,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchViewPreview() {
    val textState = remember { mutableStateOf(TextFieldValue("")) }
    SearchComposable()
}
