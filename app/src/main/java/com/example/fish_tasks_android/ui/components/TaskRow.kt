package com.example.fish_tasks_android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fish_tasks_android.R
import com.example.fish_tasks_android.data.AppSettings
import com.example.fish_tasks_android.model.Task
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskRow(
    task: Task,
    currentTime: Long,
    settings: AppSettings,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onComplete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onDelete()
                    true
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> Color.LightGray
                    SwipeToDismissBoxValue.StartToEnd -> Color.Red
                    SwipeToDismissBoxValue.EndToStart -> Color.Green
                }
            )
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Delete
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Check
                else -> null
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                icon?.let { Icon(it, contentDescription = null, tint = Color.White) }
            }
        },
        content = {
            TaskRowContent(task, currentTime, settings, onClick)
        }
    )
}

@Composable
fun TaskRowContent(
    task: Task,
    currentTime: Long,
    settings: AppSettings,
    onClick: () -> Unit
) {
    val progress = calculateProgress(task, currentTime)
    val isHistorical = task.status != "active"
    val backgroundColor = if (isHistorical) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = androidx.compose.ui.graphics.RectangleShape
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(modifier = Modifier.size(40.dp)) {
                val fishRes = when (task.priority) {
                    "low" -> R.drawable.fish_row3
                    "medium" -> R.drawable.fish_row2
                    "high" -> R.drawable.fish_row0
                    else -> R.drawable.fish_row3
                }
                AsyncImage(
                    model = fishRes,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                val categoryName = when (task.category) {
                    "top" -> settings.catTop.takeIf { it.isNotEmpty() } ?: stringResource(R.string.work)
                    "right" -> settings.catRight.takeIf { it.isNotEmpty() } ?: stringResource(R.string.project)
                    "bottom" -> settings.catBottom.takeIf { it.isNotEmpty() } ?: stringResource(R.string.personal)
                    "left" -> settings.catLeft.takeIf { it.isNotEmpty() } ?: stringResource(R.string.shopping)
                    else -> ""
                }
                Text(
                    text = "$categoryName · ${Math.round(progress * 100)}% ${stringResource(R.string.app_name)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = getRemainingText(task, currentTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status icon
            if (task.workStatus == "in_progress") {
                Text("🎣", fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun getRemainingText(task: Task, currentTime: Long): String {
    if (task.status == "completed") {
        val date = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(task.completedAt ?: 0))
        return "${stringResource(R.string.status_completed)}: $date"
    }
    if (task.status == "deleted") {
        val date = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(task.deletedAt ?: 0))
        return "${stringResource(R.string.status_deleted)}: $date"
    }
    val remaining = task.deadline - currentTime
    return when {
        remaining <= 0 -> stringResource(R.string.overdue_label)
        remaining >= 24 * 3600 * 1000 -> stringResource(R.string.days_left, remaining / (24 * 3600 * 1000))
        remaining >= 3600 * 1000 -> stringResource(R.string.hours_left, remaining / (3600 * 1000))
        else -> stringResource(R.string.minutes_left, remaining / 60000)
    }
}
