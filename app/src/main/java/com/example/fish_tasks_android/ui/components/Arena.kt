package com.example.fish_tasks_android.ui.components

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fish_tasks_android.R
import com.example.fish_tasks_android.data.AppSettings
import com.example.fish_tasks_android.model.Task

@Composable
fun Arena(
    tasks: List<Task>,
    settings: AppSettings,
    currentTime: Long,
    onFishClick: (String) -> Unit,
    onFishLongClick: (String) -> Unit
) {
    val context = LocalContext.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.Black)
    ) {
        val width = constraints.maxWidth
        val height = constraints.maxHeight

        // Background GIF
        AsyncImage(
            model = R.drawable.background_loop,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Category Labels
        CategoryLabel(
            text = settings.catTop.takeIf { it.isNotEmpty() } ?: stringResource(R.string.work),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
        )
        CategoryLabel(
            text = settings.catRight.takeIf { it.isNotEmpty() } ?: stringResource(R.string.project),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
        )
        CategoryLabel(
            text = settings.catBottom.takeIf { it.isNotEmpty() } ?: stringResource(R.string.personal),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        )
        CategoryLabel(
            text = settings.catLeft.takeIf { it.isNotEmpty() } ?: stringResource(R.string.shopping),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
        )

        // Fisherman in the center
        AsyncImage(
            model = R.drawable.fisherman_anim,
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.Center)
        )

        // Fish
        tasks.forEach { task ->
            val progress = calculateProgress(task, currentTime)
            val point = calculatePathPoint(task.category, progress)
            
            FishSprite(
                task = task,
                progress = progress,
                settings = settings,
                modifier = Modifier
                    .offset(
                        x = (point.first * maxWidth.value / 100).dp - 20.dp,
                        y = (point.second * maxHeight.value / 100).dp - 20.dp
                    )
                    .size(40.dp)
                    .pointerInput(task.id) {
                        detectTapGestures(
                            onTap = { onFishClick(task.id) },
                            onLongPress = { onFishLongClick(task.id) }
                        )
                    }
            )
        }
    }
}

@Composable
fun CategoryLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 12.sp,
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
fun FishSprite(
    task: Task,
    progress: Float,
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    val fishRes = when (task.priority) {
        "low" -> R.drawable.fish_row3
        "medium" -> R.drawable.fish_row2
        "high" -> R.drawable.fish_row0
        else -> R.drawable.fish_row3
    }
    
    val isAlert = when (task.priority) {
        "low" -> settings.alertLow
        "medium" -> settings.alertMedium
        "high" -> settings.alertHigh
        else -> true
    } && (progress * 100) >= settings.alertThreshold

    Box(modifier = modifier) {
        AsyncImage(
            model = fishRes,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        if (task.workStatus == "in_progress") {
            Text("🎣", modifier = Modifier.align(Alignment.TopEnd).offset(x = 5.dp, y = (-5).dp))
        } else if (isAlert) {
            Image(
                painter = painterResource(R.drawable.exclamation_warning_round_red_icon),
                contentDescription = null,
                modifier = Modifier.size(15.dp).align(Alignment.TopEnd).offset(x = 5.dp, y = (-5).dp)
            )
        }
    }
}

fun calculateProgress(task: Task, currentTime: Long): Float {
    val total = (task.deadline - task.createdAt).coerceAtLeast(1)
    return ((currentTime - task.createdAt).toFloat() / total).coerceIn(0f, 1f)
}

fun calculatePathPoint(category: String, progress: Float): Pair<Float, Float> {
    val start = when (category) {
        "top" -> Pair(50f, 10f)
        "right" -> Pair(90f, 50f)
        "bottom" -> Pair(50f, 90f)
        "left" -> Pair(10f, 50f)
        else -> Pair(50f, 10f)
    }
    val target = Pair(50f, 50f)
    
    return Pair(
        start.first + (target.first - start.first) * progress,
        start.second + (target.second - start.second) * progress
    )
}
