package com.example.todo91.taskdetail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todo91.model.Todo
import com.example.todo91.ui.theme.AppColors
import com.example.todo91.ui.theme.ToDo91Theme
import com.example.todo91.viewmodel.TodoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    todoId: String?,
    onBackClick: () -> Unit,
    todoViewModel: TodoViewModel = viewModel()
) {
    val todo by if (todoId != null) {
        todoViewModel.getTodoById(todoId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf<Todo?>(null) }
    }

    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var taskText by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableStateOf(0) }
    var lastEditedString by remember { mutableStateOf(" ") }
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(todo, todoId) {
        if (todoId != null) { // Edit mode
            if (todo != null) {
                title = todo!!.title
                taskText = todo!!.task
                selectedColorIndex = todo!!.colorIndex
                lastEditedString = todo!!.lastEdited?.toDate()?.let { formatDate(it) } ?: "Just now"
                isInitialized = true
            }
        } else { // Add mode
            if (!isInitialized) {
                selectedColorIndex = AppColors.TaskBackgroundColors.indices.random()
                lastEditedString = "Creating new note..."
                isInitialized = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (todoId == null) "Add New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (todo != null) {
                        IconButton(onClick = {
                            val sharedText = if (title.isNotBlank()) "$title\n\n$taskText" else taskText
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, sharedText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share Note")
                        }

                        IconButton(onClick = {
                            todoViewModel.toggleArchiveStatus(todo!!)
                            onBackClick()
                        }) {
                            Icon(Icons.Filled.Archive, contentDescription = "Archive Note")
                        }

                        IconButton(onClick = { todoViewModel.togglePinStatus(todo!!) }) {
                            Icon(
                                imageVector = if (todo?.isPinned == true) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Pin Note"
                            )
                        }
                    }

                    IconButton(onClick = {
                        if (taskText.isNotBlank() || title.isNotBlank()) {
                            if (todo != null) { // Update existing task
                                todoViewModel.updateTodo(
                                    todo!!.copy(
                                        title = title,
                                        task = taskText,
                                        colorIndex = selectedColorIndex
                                    )
                                )
                            } else { // Add new task
                                todoViewModel.addTodo(title, taskText, selectedColorIndex)
                            }
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save Note")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = taskText,
                    onValueChange = { taskText = it },
                    label = { Text("Note content...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Select Color:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(AppColors.TaskBackgroundColors) { index, color ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = 2.dp,
                                    color = if (index == selectedColorIndex) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (index == selectedColorIndex) {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            Text(
                text = "Edited: $lastEditedString",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return sdf.format(date)
}

@Preview(showBackground = true)
@Composable
fun PreviewTaskDetailScreen() {
    ToDo91Theme {
        TaskDetailScreen(todoId = null, onBackClick = {})
    }
}