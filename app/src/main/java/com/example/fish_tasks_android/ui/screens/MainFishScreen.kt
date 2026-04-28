package com.example.fish_tasks_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fish_tasks_android.FishViewModel
import com.example.fish_tasks_android.R
import com.example.fish_tasks_android.ui.components.Arena
import com.example.fish_tasks_android.ui.components.TaskRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFishScreen(
    viewModel: FishViewModel,
    navController: NavController
) {
    val tasks by viewModel.allTasks.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val expiredTask by viewModel.expiredTask.collectAsState()

    var targetFilter by remember { mutableStateOf("all") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.header_subtitle), style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.alert_settings))
                    }
                    val activeCount = tasks.count { it.status == "active" }
                    val activeText = if (activeCount == 1) {
                        stringResource(R.string.active_count_singular, activeCount)
                    } else {
                        stringResource(R.string.active_count_plural, activeCount)
                    }
                    Badge { Text(activeText) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add") }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_task))
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Arena(
                tasks = tasks.filter { it.status == "active" },
                settings = settings,
                currentTime = currentTime,
                onFishClick = { taskId: String -> navController.navigate("edit/$taskId") },
                onFishLongClick = { taskId: String ->
                    tasks.find { it.id == taskId }?.let { viewModel.toggleWorkStatus(it) }
                }
            )

            // Filter Chips
            val activeTasks = tasks.filter { it.status == "active" }
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(
                    selected = targetFilter == "all",
                    onClick = { targetFilter = "all" },
                    label = { Text("${stringResource(R.string.all)} (${activeTasks.size})") }
                )
                FilterChip(
                    selected = targetFilter == "low",
                    onClick = { targetFilter = "low" },
                    label = { Text("${stringResource(R.string.common)} (${activeTasks.count { it.priority == "low" }})") }
                )
                FilterChip(
                    selected = targetFilter == "medium",
                    onClick = { targetFilter = "medium" },
                    label = { Text("${stringResource(R.string.aggressive)} (${activeTasks.count { it.priority == "medium" }})") }
                )
                FilterChip(
                    selected = targetFilter == "high",
                    onClick = { targetFilter = "high" },
                    label = { Text("${stringResource(R.string.legendary)} (${activeTasks.count { it.priority == "high" }})") }
                )
            }

            // Task List
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                val filteredTasks = tasks.filter { it.status == "active" }
                    .filter { targetFilter == "all" || it.priority == targetFilter }
                    .sortedByDescending { 
                        // Simplified relevance sorting
                        (currentTime - it.createdAt).toDouble() / (it.deadline - it.createdAt)
                    }

                items(filteredTasks, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        currentTime = currentTime,
                        settings = settings,
                        onClick = { navController.navigate("edit/${task.id}") },
                        onComplete = { viewModel.completeTask(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }

    expiredTask?.let { task ->
        AlertDialog(
            onDismissRequest = { /* Don't allow dismissal by tapping outside */ },
            title = { Text(stringResource(R.string.expired_task_title)) },
            text = { Text(stringResource(R.string.expired_text, task.title)) },
            confirmButton = {
                Button(onClick = { viewModel.dismissExpiration(true, task) }) {
                    Text(stringResource(R.string.complete_late))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissExpiration(false, task) }) {
                    Text(stringResource(R.string.delete_task), color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainFishScreenPreview() {
    // Note: This would need a mock ViewModel or a way to provide fake data
}
