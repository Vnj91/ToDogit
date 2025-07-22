package com.example.todo91.taskdetail

import android.app.Application // Needed for ViewModelProvider.AndroidViewModelFactory
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todo91.data.Todo
import com.example.todo91.ui.theme.ToDo91Theme
import com.example.todo91.viewmodel.TodoViewModel
import com.example.todo91.ui.theme.AppColors // For new task color
import java.util.UUID // For new task UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    todoId: String?, // The ID of the task to edit, nullable for new tasks
    onBackClick: () -> Unit,
    todoViewModel: TodoViewModel = viewModel() // Use the main TodoViewModel
) {
    // Local states for the UI elements
    var taskText by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }
    var initialTodo: Todo? by remember { mutableStateOf(null) } // To hold the initial Todo object if editing

    // Collect all todos from the ViewModel to find the one being edited
    val allTodos by todoViewModel.todos.collectAsState(initial = emptyList())

    // LaunchedEffect to initialize states when todoId changes or when allTodos are loaded
    LaunchedEffect(todoId, allTodos) {
        if (todoId != null && todoId != "null") {
            // It's an existing task, try to find it
            val foundTodo = allTodos.find { it.id.toString() == todoId }
            if (foundTodo != null) {
                initialTodo = foundTodo // Store the original Todo object
                taskText = foundTodo.task
                isCompleted = foundTodo.isCompleted
            } else {
                // Task not found (e.g., deleted by another user), go back
                onBackClick()
            }
        } else {
            // It's a new task
            initialTodo = null
            taskText = ""
            isCompleted = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (todoId == null || todoId == "null") "Add New Task" else "Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (taskText.isNotBlank()) {
                            if (initialTodo != null) {
                                // Update existing task
                                todoViewModel.updateTodo(
                                    initialTodo!!.copy(
                                        task = taskText,
                                        isCompleted = isCompleted
                                    )
                                )
                            } else {
                                // Add new task
                                // ViewModel's addTodo handles random color index
                                todoViewModel.addTodo(taskText)
                            }
                            onBackClick() // Go back after saving
                        }
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save Task")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = taskText,
                onValueChange = { taskText = it },
                label = { Text("Task Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { isCompleted = it }
                )
                Text("Completed")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTaskDetailScreen() {
    ToDo91Theme {
        TaskDetailScreen(todoId = null, onBackClick = {}) // Preview for adding new task
    }
}