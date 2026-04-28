package com.example.fish_tasks_android.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fish_tasks_android.FishViewModel
import com.example.fish_tasks_android.R
import com.example.fish_tasks_android.model.Task
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    viewModel: FishViewModel,
    taskId: String?,
    onBack: () -> Unit
) {
    val tasks by viewModel.allTasks.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    val task = remember(taskId, tasks) { tasks.find { it.id == taskId } }

    if (task == null) {
        onBack()
        return
    }

    var title by remember { mutableStateOf(task.title) }
    var category by remember { mutableStateOf(task.category) }
    var priority by remember { mutableStateOf(task.priority) }
    var description by remember { mutableStateOf(task.description) }
    var workStatus by remember { mutableStateOf(task.workStatus) }
    
    val calendar = remember { Calendar.getInstance().apply { timeInMillis = task.deadline } }
    var deadline by remember { mutableLongStateOf(task.deadline) }

    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.adjust_task)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.task_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = task.status == "active"
            )

            // Category
            Text(stringResource(R.string.category_label), style = MaterialTheme.typography.labelLarge)
            val categories = listOf("top", "right", "bottom", "left")
            val categoryNames = listOf(
                settings.catTop.takeIf { it.isNotEmpty() } ?: stringResource(R.string.work),
                settings.catRight.takeIf { it.isNotEmpty() } ?: stringResource(R.string.project),
                settings.catBottom.takeIf { it.isNotEmpty() } ?: stringResource(R.string.personal),
                settings.catLeft.takeIf { it.isNotEmpty() } ?: stringResource(R.string.shopping)
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEachIndexed { index, cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(categoryNames[index]) },
                        modifier = Modifier.weight(1f),
                        enabled = task.status == "active"
                    )
                }
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = task.status == "active"
            )

            // Status Dropdown (only for active tasks)
            if (task.status == "active") {
                Text(stringResource(R.string.status_pending), style = MaterialTheme.typography.labelLarge) // Label placeholder
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (workStatus == "in_progress") stringResource(R.string.status_in_progress) else stringResource(R.string.status_pending))
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.status_pending)) },
                            onClick = { workStatus = "pending"; expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.status_in_progress)) },
                            onClick = { workStatus = "in_progress"; expanded = false }
                        )
                    }
                }
            }

            // Priority
            Text(stringResource(R.string.choose_a_fish), style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PriorityChoice(
                    priorityName = "low",
                    isSelected = priority == "low",
                    onClick = { if (task.status == "active") priority = "low" },
                    modifier = Modifier.weight(1f)
                )
                PriorityChoice(
                    priorityName = "medium",
                    isSelected = priority == "medium",
                    onClick = { if (task.status == "active") priority = "medium" },
                    modifier = Modifier.weight(1f)
                )
                PriorityChoice(
                    priorityName = "high",
                    isSelected = priority == "high",
                    onClick = { if (task.status == "active") priority = "high" },
                    modifier = Modifier.weight(1f)
                )
            }

            // Deadline
            Text(stringResource(R.string.due_date), style = MaterialTheme.typography.labelLarge)
            Button(
                onClick = {
                    val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.MONTH, month)
                        calendar.set(Calendar.DAY_OF_MONTH, day)
                        
                        TimePickerDialog(context, { _, hour, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
                            deadline = calendar.timeInMillis
                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                    }
                    DatePickerDialog(context, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(sdf.format(Date(deadline)))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            if (task.status == "active") {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.updateTask(task.copy(
                                title = title,
                                category = category,
                                priority = priority,
                                deadline = deadline,
                                description = description,
                                workStatus = workStatus
                            ))
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_action))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.completeTask(task); onBack() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.White)
                    ) {
                        Text(stringResource(R.string.complete_action))
                    }
                    Button(
                        onClick = { viewModel.deleteTask(task); onBack() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                    ) {
                        Text(stringResource(R.string.delete_action))
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.restoreTask(task, deadline); onBack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.restore_action))
                }
                Button(
                    onClick = { viewModel.deleteTask(task); onBack() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                ) {
                    Text(stringResource(R.string.delete_action))
                }
            }
        }
    }
}
