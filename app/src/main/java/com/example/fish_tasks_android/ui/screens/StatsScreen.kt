package com.example.fish_tasks_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fish_tasks_android.FishViewModel
import com.example.fish_tasks_android.R

@Composable
fun StatsScreen(viewModel: FishViewModel) {
    val settings by viewModel.settings.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()

    val total = settings.completed + settings.expired + settings.deleted
    val rate = if (total > 0) (settings.completed * 100 / total) else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.stats), style = MaterialTheme.typography.headlineMedium)

        // Main Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(stringResource(R.string.score_label), settings.score.toString(), Modifier.weight(1f))
            StatCard(stringResource(R.string.streak_label), settings.streak.toString(), Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(stringResource(R.string.status_completed), settings.completed.toString(), Modifier.weight(1f))
            StatCard(stringResource(R.string.expired_label), settings.expired.toString(), Modifier.weight(1f))
            StatCard(stringResource(R.string.deleted_label), settings.deleted.toString(), Modifier.weight(1f))
        }
        StatCard(stringResource(R.string.completion_rate), "$rate%", Modifier.fillMaxWidth())

        // Insights (Simplified)
        Text(stringResource(R.string.completed_by_state), style = MaterialTheme.typography.titleLarge)
        val completedTasks = tasks.filter { it.status == "completed" }
        val states = listOf("in_progress", "priority", "urgent", "pending")
        val stateCounts = states.associateWith { key ->
            completedTasks.count { it.completedStateKey == key }
        }

        stateCounts.forEach { (state, count) ->
            val label = when (state) {
                "in_progress" -> stringResource(R.string.status_in_progress)
                "priority" -> stringResource(R.string.filter_priority)
                "urgent" -> "Urgent" // Placeholder
                else -> stringResource(R.string.status_pending)
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(label, modifier = Modifier.weight(1f))
                Text(count.toString(), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = if (completedTasks.isNotEmpty()) count.toFloat() / completedTasks.size else 0f,
                    modifier = Modifier.width(100.dp).height(8.dp)
                )
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}
