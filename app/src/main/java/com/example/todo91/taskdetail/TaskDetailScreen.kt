package com.example.todo91.taskdetail

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todo91.common.formatDate
import com.example.todo91.model.Todo
import com.example.todo91.reminders.AlarmSchedulerImpl
import com.example.todo91.ui.theme.AppColors
import com.example.todo91.viewmodel.TodoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
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

    val colors = if (isSystemInDarkTheme()) AppColors.DarkThemeTaskColors else AppColors.LightThemeTaskColors

    var title by remember(todo?.id) { mutableStateOf(todo?.title ?: "") }
    var taskText by remember(todo?.id) { mutableStateOf(todo?.task ?: "") }
    var selectedColorIndex by remember(todo?.id) { mutableStateOf(todo?.colorIndex ?: colors.indices.random()) }
    val lastEditedString = todo?.lastEdited?.toDate()?.let { formatDate(it) } ?: "Just now"

    val animatedBackgroundColor by animateColorAsState(
        targetValue = colors.getOrElse(selectedColorIndex) { colors.first() },
        label = "background_color_animation"
    )

    val isDarkBackground = animatedBackgroundColor.red * 0.299 + animatedBackgroundColor.green * 0.587 + animatedBackgroundColor.blue * 0.114 < 0.5

    val context = LocalContext.current
    val alarmScheduler = remember { AlarmSchedulerImpl(context) }

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        } ?: LocalDate.now()
                        val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        val localDateTime = selectedDate.atTime(selectedTime)
                        val instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant()
                        val newReminderTime = Timestamp(Date.from(instant))

                        todo?.let {
                            val updatedTodo = it.copy(reminderTime = newReminderTime)
                            todoViewModel.updateTodo(updatedTodo)
                            alarmScheduler.schedule(updatedTodo)
                        }
                        showTimePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        showTimePicker = true
                    }
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = animatedBackgroundColor,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = if (isDarkBackground) Color.White else Color.Black,
                    titleContentColor = if (isDarkBackground) Color.White else Color.Black,
                    actionIconContentColor = if (isDarkBackground) Color.White else Color.Black
                ),
                title = { Text(if (todoId == null) "Add New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val currentTodo = todo
                    if (currentTodo != null) {
                        IconButton(onClick = {
                            if (notificationPermissionState == null || notificationPermissionState.status.isGranted) {
                                showDatePicker = true
                            } else {
                                notificationPermissionState.launchPermissionRequest()
                            }
                        }) {
                            Icon(
                                imageVector = if (currentTodo.reminderTime != null) Icons.Filled.NotificationsActive else Icons.Filled.Notifications,
                                contentDescription = "Add Reminder"
                            )
                        }
                        IconButton(onClick = {
                            todoViewModel.toggleArchiveStatus(currentTodo)
                            onBackClick()
                        }) {
                            Icon(Icons.Filled.Archive, contentDescription = "Archive Note")
                        }
                        IconButton(onClick = {
                            todoViewModel.togglePinStatus(currentTodo)
                        }) {
                            Icon(
                                imageVector = if (currentTodo.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Pin Note"
                            )
                        }
                    }

                    IconButton(onClick = {
                        if (taskText.isNotBlank() || title.isNotBlank()) {
                            if (currentTodo != null) {
                                todoViewModel.updateTodo(
                                    currentTodo.copy(
                                        title = title,
                                        task = taskText,
                                        colorIndex = selectedColorIndex
                                    )
                                )
                            } else {
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
                    textStyle = MaterialTheme.typography.titleLarge,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = if (isDarkBackground) Color.White else MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = if (isDarkBackground) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        cursorColor = if (isDarkBackground) Color.White else MaterialTheme.colorScheme.primary,
                        focusedLabelColor = if (isDarkBackground) Color.White else MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = if (isDarkBackground) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = taskText,
                    onValueChange = { taskText = it },
                    label = { Text("Note content...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = if (isDarkBackground) Color.White else MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = if (isDarkBackground) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        cursorColor = if (isDarkBackground) Color.White else MaterialTheme.colorScheme.primary,
                        focusedLabelColor = if (isDarkBackground) Color.White else MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = if (isDarkBackground) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Select Color:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = if (isDarkBackground) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(colors) { index, color ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = 2.dp,
                                    color = if (index == selectedColorIndex) {
                                        if (isDarkBackground) Color.White else MaterialTheme.colorScheme.primary
                                    } else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (index == selectedColorIndex) {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = "Selected",
                                    tint = if (isDarkBackground) Color.White else MaterialTheme.colorScheme.primary
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
                color = if (isDarkBackground) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
