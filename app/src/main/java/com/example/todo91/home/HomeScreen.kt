package com.example.todo91.home

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todo91.common.*
import com.example.todo91.model.Todo
import com.example.todo91.viewmodel.TodoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
    onNavigateToTaskDetail: (String?) -> Unit
) {
    val todoViewModel: TodoViewModel = viewModel()
    val todos by todoViewModel.todos.collectAsState(initial = emptyList())
    val currentSortOrder by todoViewModel.currentSortOrder.collectAsState()
    val isLoading by todoViewModel.isLoading.collectAsState()
    val errorLoadingTasks by todoViewModel.errorLoadingTasks.collectAsState()

    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    val isInSelectionMode = selectedIds.isNotEmpty()

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentFilterOrder by remember { mutableStateOf(FilterOrder.ALL) }
    var showFilterMenu by remember { mutableStateOf(false) }

    val filteredTodos = remember(todos, searchQuery, currentFilterOrder) {
        filterNotes(todos, searchQuery, currentFilterOrder)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AnimatedContent(targetState = isInSelectionMode, label = "TopAppBar Animation") { inSelectionMode ->
                if (inSelectionMode) {
                    ContextualTopAppBar(
                        selectedItemCount = selectedIds.size,
                        onCloseClick = { selectedIds = emptySet() },
                        onPinClick = {
                            todoViewModel.pinTodos(selectedIds, true)
                            selectedIds = emptySet()
                        },
                        onArchiveClick = {
                            val idsToArchive = selectedIds
                            todoViewModel.archiveTodos(idsToArchive, true)
                            selectedIds = emptySet()
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "${idsToArchive.size} notes archived",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Long
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    todoViewModel.archiveTodos(idsToArchive, false)
                                }
                            }
                        },
                        onDeleteClick = { showDeleteConfirmationDialog = true }
                    )
                } else {
                    HomeTopBar(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onSearchClick = { isSearchActive = true },
                        onSortClick = { todoViewModel.setSortOrder(it) },
                        onFilterClick = { showFilterMenu = true },
                        isSearchActive = isSearchActive,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onSearchClose = { isSearchActive = false; searchQuery = "" },
                        currentSortOrder = currentSortOrder,
                        showFilterMenu = showFilterMenu,
                        onDismissFilterMenu = { showFilterMenu = false },
                        onSelectFilter = { currentFilterOrder = it }
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(visible = !isInSelectionMode, enter = fadeIn(), exit = fadeOut()) {
                FloatingActionButton(onClick = { onNavigateToTaskDetail(null) }) {
                    Icon(Icons.Filled.Add, "Add new note")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            when {
                isLoading -> LoadingScreen()
                errorLoadingTasks != null -> ErrorScreen(message = errorLoadingTasks!!)
                filteredTodos.isEmpty() -> EmptyScreen(searchQuery = searchQuery, currentFilterOrder = currentFilterOrder)
                else -> NoteGrid(
                    todos = filteredTodos,
                    selectedIds = selectedIds,
                    onNoteClick = { todo ->
                        if (isInSelectionMode) {
                            if (todo.id in selectedIds) {
                                selectedIds -= todo.id!!
                            } else {
                                selectedIds += todo.id!!
                            }
                        } else {
                            onNavigateToTaskDetail(todo.id)
                        }
                    },
                    onNoteLongClick = { todo ->
                        if (!isInSelectionMode) {
                            selectedIds += todo.id!!
                        }
                    }
                )
            }
        }
    }

    if (showDeleteConfirmationDialog) {
        ConfirmationDialog(
            title = "Delete ${selectedIds.size} notes?",
            message = "Are you sure you want to permanently delete these notes?",
            onConfirm = {
                todoViewModel.deleteTodos(selectedIds)
                showDeleteConfirmationDialog = false
                selectedIds = emptySet()
            },
            onDismiss = { showDeleteConfirmationDialog = false }
        )
    }
}

private fun filterNotes(notes: List<Todo>, searchQuery: String, filterOrder: FilterOrder): List<Todo> {
    val searchFiltered = if (searchQuery.isBlank()) {
        notes
    } else {
        notes.filter { it.task.contains(searchQuery, ignoreCase = true) || it.title.contains(searchQuery, ignoreCase = true) }
    }
    return when (filterOrder) {
        FilterOrder.COMPLETED -> searchFiltered.filter { it.isCompleted }
        FilterOrder.INCOMPLETE -> searchFiltered.filter { !it.isCompleted }
        FilterOrder.ALL -> searchFiltered
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteGrid(
    todos: List<Todo>,
    selectedIds: Set<String>,
    onNoteClick: (Todo) -> Unit,
    onNoteLongClick: (Todo) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val columnCount = when {
        screenWidth > 840.dp -> 4
        screenWidth > 600.dp -> 3
        else -> 2
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columnCount),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp
    ) {
        items(todos, key = { it.id!! }) { todo ->
            TodoItem(
                modifier = Modifier.animateItemPlacement(),
                todo = todo,
                isSelected = todo.id in selectedIds,
                onClick = { onNoteClick(todo) },
                onLongClick = { onNoteLongClick(todo) }
            )
        }
    }
}