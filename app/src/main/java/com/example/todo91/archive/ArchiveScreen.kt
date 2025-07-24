package com.example.todo91.archive

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todo91.common.EmptyScreen
import com.example.todo91.common.LoadingScreen
import com.example.todo91.common.TodoItem
import com.example.todo91.viewmodel.TodoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ArchiveScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
    onNavigateToTaskDetail: (String?) -> Unit
) {
    val todoViewModel: TodoViewModel = viewModel()
    val archivedTodos by todoViewModel.archivedTodos.collectAsState(initial = emptyList())
    val isLoading by todoViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            ArchiveTopBar(onMenuClick = { scope.launch { drawerState.open() } })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading && archivedTodos.isEmpty()) {
                LoadingScreen()
            } else if (archivedTodos.isEmpty()) {
                EmptyScreen(isArchive = true)
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    // REMOVED: The invalid verticalArrangement parameter was here. It's now gone.
                    verticalItemSpacing = 8.dp // ADDED: This is the correct parameter.
                ) {
                    items(archivedTodos, key = { it.id!! }) { todo ->
                        TodoItem(
                            modifier = Modifier.animateItemPlacement(),
                            todo = todo,
                            isSelected = false,
                            onClick = { onNavigateToTaskDetail(todo.id) },
                            onLongClick = {}
                        )
                    }
                }
            }
        }
    }
}