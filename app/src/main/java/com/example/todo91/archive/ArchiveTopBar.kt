package com.example.todo91.archive

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text("Archive") },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
            }
        }
    )
}