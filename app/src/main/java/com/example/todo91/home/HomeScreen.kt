package com.example.todo91.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
// REMOVED: import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todo91.data.Todo
import com.example.todo91.ui.theme.ToDo91Theme
import com.example.todo91.home.TodoTopBar
import com.example.todo91.viewmodel.TodoViewModel
import com.example.todo91.data.SortOrder
import com.example.todo91.ui.theme.AppColors
// REMOVED: import androidx.compose.foundation.gestures.detectTapGestures
// REMOVED: import androidx.compose.foundation.interaction.MutableInteractionSource
// REMOVED: import androidx.compose.material.ripple.rememberRipple
// REMOVED: import androidx.compose.foundation.LocalIndication


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTaskDetail: (String?) -> Unit,
    todoViewModel: TodoViewModel = viewModel()
) {
    var newTaskText by remember { mutableStateOf("") }
    val todos by todoViewModel.todos.collectAsState(initial = emptyList())
    val currentSortOrder by todoViewModel.currentSortOrder.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTodos = remember(todos, searchQuery) {
        if (searchQuery.isBlank()) {
            todos
        } else {
            todos.filter { it.task.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TodoTopBar(
                onSearchClick = { isSearchActive = true },
                onSortClick = { sortOrder -> todoViewModel.setSortOrder(sortOrder) },
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchClose = {
                    isSearchActive = false
                    searchQuery = ""
                },
                isSearchActive = isSearchActive
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onNavigateToTaskDetail(null)
            }) {
                Icon(Icons.Filled.Add, "Add new task")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (filteredTodos.isEmpty()) {
                Text(
                    text = "No tasks found! Add one using the + button.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTodos, key = { it.id }) { todo ->
                        TodoItem(
                            todo = todo,
                            onToggleComplete = { todoViewModel.toggleTodoCompletion(todo) },
                            onDelete = { todoViewModel.deleteTodo(todo) },
                            onEdit = {
                                println("DEBUG: Card clicked for task ID: ${todo.id}")
                                onNavigateToTaskDetail(todo.id.toString())
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TodoItem(
    todo: Todo,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(140.dp)
            .clickable(onClick = onEdit), // Revert to standard clickable for both ripple and click
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.TaskBackgroundColors[todo.colorIndex])
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Ensure Column fills the entire Card
                .padding(16.dp), // Padding is applied *after* clickable, so it's internal
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = todo.task,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null
                    ),
                    modifier = Modifier.weight(1f)
                )
                // These IconButton and Checkbox should consume their own clicks,
                // allowing the parent Card's clickable to work for the rest of the area.
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete task")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Checkbox(
                    checked = todo.isCompleted,
                    onCheckedChange = { onToggleComplete() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    ToDo91Theme {
        HomeScreen(onNavigateToTaskDetail = {})
    }
}

fun String.toColor(): Color {
    var hex = this.removePrefix("#")
    if (hex.length == 6) {
        hex = "FF$hex"
    } else if (hex.length != 8) {
        println("ERROR: Invalid hex color string length: $this")
        return Color.Black
    }
    val argbValue = hex.toULong(16)
    return Color(argbValue)
}