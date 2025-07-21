package com.example.todo91.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todo91.ui.theme.ToDo91Theme
import com.example.todo91.ui.home.TodoTopBar

data class Todo(
    val id: String = java.util.UUID.randomUUID().toString(),
    val task: String,
    var isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var newTaskText by remember { mutableStateOf("") }

    val todos = remember {
        mutableStateListOf(
            Todo(task = "Buy groceries"),
            Todo(task = "Call Mom", isCompleted = true),
            Todo(task = "Finish Compose tutorial"),
            Todo(task = "Read a book"),
            Todo(task = "Exercise"),
            Todo(task = "Learn Kotlin")
        )
    }

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Filtered tasks based on search query
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
                onSortClick = { println("Sort Clicked!") /* TODO: Implement sorting logic */ },
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchClose = {
                    isSearchActive = false
                    searchQuery = ""
                },
                isSearchActive = isSearchActive
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    label = { Text("New Task") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    if (newTaskText.isNotBlank()) {
                        todos.add(Todo(task = newTaskText))
                        newTaskText = ""
                    }
                }) {
                    Icon(Icons.Filled.Add, "Add new task")
                }
            }

            if (filteredTodos.isEmpty()) {
                Text(
                    text = "No tasks found! Add one or adjust search.",
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
                            onToggleComplete = {
                                val index = todos.indexOf(todo)
                                if (index != -1) {
                                    todos[index] = todo.copy(isCompleted = !todo.isCompleted)
                                }
                                println("Toggled completion for task: ${todo.task}")
                            },
                            onDelete = {
                                println("Delete action for task: ${todo.task}")
                                todos.remove(todo)
                            },
                            onEdit = {
                                println("Edit action for task: ${todo.task}")
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
            .aspectRatio(1.2f)
            .heightIn(min = 100.dp)
            .clickable(onClick = onEdit),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Checkbox(
                    checked = todo.isCompleted,
                    onCheckedChange = { onToggleComplete() }
                )
                Text(
                    text = todo.task,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null // Strikethrough
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete task")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    ToDo91Theme {
        HomeScreen()
    }
}