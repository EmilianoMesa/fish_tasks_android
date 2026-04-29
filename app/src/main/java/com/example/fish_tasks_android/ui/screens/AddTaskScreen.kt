package com.example.fish_tasks_android.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fish_tasks_android.FishViewModel
import com.example.fish_tasks_android.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: FishViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("top") }
    var priority by remember { mutableStateOf("low") }
    var description by remember { mutableStateOf("") }
    
    val calendar = remember { Calendar.getInstance().apply { add(Calendar.HOUR, 1) } }
    var deadline by remember { mutableLongStateOf(calendar.timeInMillis) }

    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_new_task)) },
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
                placeholder = { Text(stringResource(R.string.task_title_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("${stringResource(R.string.description_label)} ${stringResource(R.string.optional)}") },
                placeholder = { Text(stringResource(R.string.description_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Priority (Fish)
            Text(stringResource(R.string.choose_a_fish), style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PriorityChoice(
                    priorityName = "low",
                    isSelected = priority == "low",
                    onClick = { priority = "low" },
                    modifier = Modifier.weight(1f)
                )
                PriorityChoice(
                    priorityName = "medium",
                    isSelected = priority == "medium",
                    onClick = { priority = "medium" },
                    modifier = Modifier.weight(1f)
                )
                PriorityChoice(
                    priorityName = "high",
                    isSelected = priority == "high",
                    onClick = { priority = "high" },
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

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addTask(title, category, priority, deadline, description)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.add_task))
            }
        }
    }
}

@Composable
fun PriorityChoice(
    priorityName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fishRes = when (priorityName) {
        "low" -> R.drawable.fish_row3
        "medium" -> R.drawable.fish_row2
        "high" -> R.drawable.fish_row0
        else -> R.drawable.fish_row3
    }
    val fishTitle = when (priorityName) {
        "low" -> stringResource(R.string.blue_fish)
        "medium" -> stringResource(R.string.red_fish)
        "high" -> stringResource(R.string.gold_fish)
        else -> ""
    }

    Column(
        modifier = modifier
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = fishRes,
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Text(fishTitle, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
