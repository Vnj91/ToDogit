package com.example.todo91.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.todo91.home.FilterOrder
import com.example.todo91.model.Todo
import com.example.todo91.ui.theme.AppColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoItem(
    modifier: Modifier = Modifier,
    todo: Todo,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val cardBorder by animateDpAsState(if (isSelected) 2.dp else 0.dp, label = "border_animation")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp)
            .border(
                width = cardBorder,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.TaskBackgroundColors[todo.colorIndex % AppColors.TaskBackgroundColors.size])
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                if (todo.title.isNotEmpty()) {
                    Text(text = todo.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                    Spacer(Modifier.height(8.dp))
                }
                if (todo.task.isNotEmpty()) {
                    Text(text = todo.task, style = MaterialTheme.typography.bodyMedium, maxLines = 10)
                }

                if (todo.isPinned) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "Pinned",
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.End)
                            .padding(top = 8.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // UPDATED: Replaced AnimatedVisibility with a simple 'if' to fix the compiler error.
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }
            }
        }
    }
}

// ... (The rest of the file remains unchanged)

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp), contentAlignment = Alignment.Center) {
        Text(text = "Error: $message", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
    }
}

@Composable
fun EmptyScreen(searchQuery: String = "", currentFilterOrder: FilterOrder = FilterOrder.ALL, isArchive: Boolean = false) {
    val message = when {
        isArchive -> "Your archived notes appear here."
        searchQuery.isNotBlank() || currentFilterOrder != FilterOrder.ALL -> "No notes match your filter."
        else -> "No notes yet! Click '+' to add one."
    }
    val icon = if (isArchive) Icons.Default.Archive else Icons.AutoMirrored.Filled.ListAlt

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Empty",
            modifier = Modifier.size(128.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}