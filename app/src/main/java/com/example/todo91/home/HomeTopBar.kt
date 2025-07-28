package com.example.todo91.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.todo91.model.SortOrder
import com.example.todo91.utils.capitalizeWords

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSortClick: (SortOrder) -> Unit,
    onFilterClick: () -> Unit,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit,
    currentSortOrder: SortOrder,
    showFilterMenu: Boolean,
    onDismissFilterMenu: () -> Unit,
    onSelectFilter: (FilterOrder) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = {
            if (isSearchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search your notes...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
            } else {
                Text("Notes")
            }
        },
        navigationIcon = {
            if (isSearchActive) {
                IconButton(onClick = onSearchClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close Search")
                }
            } else {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                }
            }
        },
        actions = {
            if (isSearchActive) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear Search")
                }
            } else {
                IconButton(onClick = onSearchClick) { Icon(Icons.Filled.Search, contentDescription = "Search Notes") }
                IconButton(onClick = onFilterClick) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Filter Notes")
                }
                DropdownMenu(expanded = showFilterMenu, onDismissRequest = onDismissFilterMenu) {
                    FilterOrder.values().forEach { filterOrder ->
                        DropdownMenuItem(
                            text = { Text(filterOrder.name.capitalizeWords()) },
                            onClick = {
                                onSelectFilter(filterOrder)
                                onDismissFilterMenu()
                            }
                        )
                    }
                }
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort Notes")
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        SortOrder.values().forEach { sortOrder ->
                            DropdownMenuItem(
                                text = { Text(sortOrder.name.capitalizeWords()) },
                                onClick = {
                                    onSortClick(sortOrder)
                                    showSortMenu = false
                                },
                                enabled = sortOrder != currentSortOrder
                            )
                        }
                    }
                }
            }
        }
    )
}
