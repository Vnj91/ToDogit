package com.example.todo91.archive

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = { Text("Archive") },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
            }
        }
    )
}