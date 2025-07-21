package com.example.todo91.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.todo91.ui.theme.ToDo91Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTopBar(onDeleteAllClick: () -> Unit) {
    TopAppBar(
        title = { Text("My List") },
        actions = {
            IconButton(onClick = onDeleteAllClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete All Tasks"
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTodoTopBar() {
    ToDo91Theme {
        TodoTopBar(onDeleteAllClick = {})
    }
}