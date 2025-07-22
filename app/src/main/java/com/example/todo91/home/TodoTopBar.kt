package com.example.todo91.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todo91.data.SortOrder
import com.example.todo91.ui.theme.ToDo91Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTopBar(
    onSearchClick: () -> Unit,
    onSortClick: (SortOrder) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit,
    isSearchActive: Boolean
) {
    var showSortMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            if (isSearchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search tasks...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
            } else {
                Text("My List")
            }
        },
        navigationIcon = {
            if (isSearchActive) {
                IconButton(onClick = onSearchClose) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Close Search")
                }
            }
        },
        actions = {
            if (isSearchActive) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Clear Search")
                }
            } else {
                IconButton(onClick = { showSortMenu = true }) { // On sort icon click, show menu
                    Icon(Icons.Filled.Sort, contentDescription = "Sort Tasks")
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOrder.values().forEach { sortOrder ->
                        DropdownMenuItem(
                            text = { Text(sortOrder.name.replace("_", " ").lowercase().capitalize()) },
                            onClick = {
                                onSortClick(sortOrder)
                                showSortMenu = false
                            }
                        )
                    }
                }
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Filled.Search, contentDescription = "Search Tasks")
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTodoTopBar() {
    ToDo91Theme {
        TodoTopBar(
            onSearchClick = {},
            onSortClick = {},
            searchQuery = "",
            onSearchQueryChange = {},
            onSearchClose = {},
            isSearchActive = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTodoTopBarSearchActive() {
    ToDo91Theme {
        TodoTopBar(
            onSearchClick = {},
            onSortClick = {},
            searchQuery = "Buy",
            onSearchQueryChange = {},
            onSearchClose = {},
            isSearchActive = true
        )
    }
}