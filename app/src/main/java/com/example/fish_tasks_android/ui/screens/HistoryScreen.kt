package com.example.fish_tasks_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fish_tasks_android.FishViewModel
import com.example.fish_tasks_android.R
import com.example.fish_tasks_android.ui.components.TaskRow

@Composable
fun HistoryScreen(viewModel: FishViewModel) {
    val tasks by viewModel.allTasks.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()

    val historicalTasks = tasks.filter { it.status == "completed" || it.status == "deleted" }
        .sortedByDescending { it.completedAt ?: it.deletedAt ?: 0L }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.history), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(historicalTasks, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    currentTime = currentTime,
                    settings = settings,
                    onClick = { /* Navigate to edit/taskId if we add it to FishApp */ },
                    onComplete = {}, // No swipe actions for history
                    onDelete = {}
                )
            }
        }
    }
}
