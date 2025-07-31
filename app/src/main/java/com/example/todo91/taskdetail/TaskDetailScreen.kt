package com.example.todo91.taskdetail

import android.Manifest
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todo91.common.formatDate
import com.example.todo91.common.isColorDark
import com.example.todo91.model.ChecklistItem
import com.example.todo91.model.Todo
import com.example.todo91.reminders.AlarmSchedulerImpl
import com.example.todo91.ui.theme.AppColors
import com.example.todo91.viewmodel.TodoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
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

    val wasArchived = remember { mutableStateOf(todo?.isArchived) }
    LaunchedEffect(todo) {
        if (wasArchived.value == false && todo?.isArchived == true) {
            onBackClick()
        }
        wasArchived.value = todo?.isArchived
    }

    val scope = rememberCoroutineScope()
    val colors = if (isSystemInDarkTheme()) AppColors.DarkThemeTaskColors else AppColors.LightThemeTaskColors

    var title by remember(todo?.id) { mutableStateOf(todo?.title ?: "") }
    var taskText by remember(todo?.id) { mutableStateOf(todo?.task ?: "") }
    var selectedColorIndex by remember(todo?.id) { mutableStateOf(todo?.colorIndex ?: colors.indices.random()) }
    val lastEditedString = todo?.lastEdited?.toDate()?.let { formatDate(it) } ?: "Just now"

    var checklistItems by remember(todo?.id) { mutableStateOf(todo?.checklistItems ?: emptyList()) }
    var isChecklistMode by remember(todo?.id) { mutableStateOf(todo?.checklistItems != null) }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = colors.getOrElse(selectedColorIndex) { colors.first() },
        label = "background_color_animation"
    )
    val contentColor = if (isColorDark(animatedBackgroundColor)) Color.White else Color.Black

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
                            scope.launch {
                                todoViewModel.updateTodo(updatedTodo)
                            }
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
                    navigationIconContentColor = contentColor,
                    titleContentColor = contentColor,
                    actionIconContentColor = contentColor
                ),
                title = { Text(if (todoId == null) "Add New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { isChecklistMode = !isChecklistMode }) {
                        Icon(
                            imageVector = if (isChecklistMode) Icons.Filled.Notes else Icons.Filled.Checklist,
                            contentDescription = if (isChecklistMode) "Switch to Note" else "Switch to Checklist"
                        )
                    }

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

                        val archiveIcon = if (currentTodo.isArchived) Icons.Filled.Unarchive else Icons.Filled.Archive
                        IconButton(onClick = {
                            scope.launch {
                                todoViewModel.toggleArchiveStatus(currentTodo)
                                // The LaunchedEffect will handle navigation now.
                            }
                        }) {
                            Icon(archiveIcon, contentDescription = "Archive Note")
                        }

                        IconButton(onClick = {
                            scope.launch {
                                todoViewModel.togglePinStatus(currentTodo)
                            }
                        }) {
                            Icon(
                                imageVector = if (currentTodo.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Pin Note"
                            )
                        }
                    }

                    IconButton(onClick = {
                        scope.launch {
                            if (taskText.isNotBlank() || title.isNotBlank() || checklistItems.any { it.text.isNotBlank() }) {
                                val finalChecklist = if (isChecklistMode) checklistItems.filter { it.text.isNotBlank() } else null
                                if (currentTodo != null) {
                                    todoViewModel.updateTodo(
                                        currentTodo.copy(
                                            title = title,
                                            task = taskText,
                                            colorIndex = selectedColorIndex,
                                            checklistItems = finalChecklist
                                        )
                                    )
                                } else {
                                    todoViewModel.addTodo(title, taskText, selectedColorIndex, finalChecklist)
                                }
                                onBackClick()
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save Note")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title") }, modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.titleLarge.copy(color = contentColor),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = contentColor,
                        unfocusedIndicatorColor = contentColor.copy(alpha = 0.7f),
                        cursorColor = contentColor,
                        focusedLabelColor = contentColor,
                        unfocusedLabelColor = contentColor.copy(alpha = 0.7f),
                        focusedTextColor = contentColor,
                        unfocusedTextColor = contentColor
                    )
                )
                Spacer(Modifier.height(8.dp))

                if (isChecklistMode) {
                    ChecklistEditor(
                        items = checklistItems,
                        onItemsChange = { checklistItems = it },
                        contentColor = contentColor
                    )
                } else {
                    OutlinedTextField(
                        value = taskText, onValueChange = { taskText = it },
                        label = { Text("Note content...") },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = contentColor,
                            unfocusedIndicatorColor = contentColor.copy(alpha = 0.7f),
                            cursorColor = contentColor,
                            focusedLabelColor = contentColor,
                            unfocusedLabelColor = contentColor.copy(alpha = 0.7f),
                            focusedTextColor = contentColor,
                            unfocusedTextColor = contentColor
                        )
                    )
                }
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Select Color:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = contentColor.copy(alpha = 0.7f)
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
                                    color = if (index == selectedColorIndex) contentColor else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (index == selectedColorIndex) {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = "Selected",
                                    tint = contentColor
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
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ChecklistEditor(
    items: List<ChecklistItem>,
    onItemsChange: (List<ChecklistItem>) -> Unit,
    contentColor: Color
) {
    LazyColumn {
        itemsIndexed(items) { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { isChecked ->
                        val newList = items.toMutableList().also {
                            it[index] = item.copy(isChecked = isChecked)
                        }
                        onItemsChange(newList)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = contentColor,
                        uncheckedColor = contentColor.copy(alpha = 0.7f),
                        checkmarkColor = MaterialTheme.colorScheme.surface
                    )
                )
                BasicTextField(
                    value = item.text,
                    onValueChange = { newText ->
                        val newList = items.toMutableList().also {
                            it[index] = item.copy(text = newText)
                        }
                        onItemsChange(newList)
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        color = contentColor,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
                    ),
                    cursorBrush = SolidColor(contentColor)
                )
                IconButton(onClick = {
                    val newList = items.toMutableList().also { it.removeAt(index) }
                    onItemsChange(newList)
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Delete item", tint = contentColor.copy(alpha = 0.7f))
                }
            }
        }
        item {
            TextButton(onClick = {
                onItemsChange(items + ChecklistItem(""))
            }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = contentColor)
                Spacer(Modifier.width(4.dp))
                Text("Add item", color = contentColor)
            }
        }
    }
}
