package com.example.fish_tasks_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fish_tasks_android.FishViewModel
import com.example.fish_tasks_android.R
import com.example.fish_tasks_android.data.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FishViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    var language by remember { mutableStateOf(settings.language) }
    var alertThreshold by remember { mutableFloatStateOf(settings.alertThreshold.toFloat()) }
    var alertLow by remember { mutableStateOf(settings.alertLow) }
    var alertMedium by remember { mutableStateOf(settings.alertMedium) }
    var alertHigh by remember { mutableStateOf(settings.alertHigh) }
    
    var catTop by remember { mutableStateOf(settings.catTop) }
    var catRight by remember { mutableStateOf(settings.catRight) }
    var catBottom by remember { mutableStateOf(settings.catBottom) }
    var catLeft by remember { mutableStateOf(settings.catLeft) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.alert_settings)) },
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
            // Language
            Text(stringResource(R.string.language_label), style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = language == "en", onClick = { language = "en" })
                Text("English")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = language == "es", onClick = { language = "es" })
                Text("Español")
            }

            // Alert Threshold
            Text("${stringResource(R.string.alert_threshold_label)}: ${alertThreshold.toInt()}%", style = MaterialTheme.typography.labelLarge)
            Slider(
                value = alertThreshold,
                onValueChange = { alertThreshold = it },
                valueRange = 1f..95f,
                steps = 94
            )

            // Alert Priorities
            Text(stringResource(R.string.alert_priorities_label), style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = alertLow, onCheckedChange = { alertLow = it })
                Text(stringResource(R.string.low_label))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = alertMedium, onCheckedChange = { alertMedium = it })
                Text(stringResource(R.string.medium_label))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = alertHigh, onCheckedChange = { alertHigh = it })
                Text(stringResource(R.string.high_label))
            }

            // Categories
            Text(stringResource(R.string.categories_label), style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(value = catTop, onValueChange = { catTop = it }, label = { Text(stringResource(R.string.top_left_label)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = catRight, onValueChange = { catRight = it }, label = { Text(stringResource(R.string.top_right_label)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = catBottom, onValueChange = { catBottom = it }, label = { Text(stringResource(R.string.bottom_right_label)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = catLeft, onValueChange = { catLeft = it }, label = { Text(stringResource(R.string.bottom_left_label)) }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.saveSettings(AppSettings(
                        language = language,
                        alertThreshold = alertThreshold.toInt(),
                        alertLow = alertLow,
                        alertMedium = alertMedium,
                        alertHigh = alertHigh,
                        catTop = catTop,
                        catRight = catRight,
                        catBottom = catBottom,
                        catLeft = catLeft,
                        score = settings.score,
                        streak = settings.streak,
                        completed = settings.completed,
                        expired = settings.expired,
                        deleted = settings.deleted
                    ))
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save_action))
            }

            // Clear History
            Text(stringResource(R.string.clear_history_label), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            Text(stringResource(R.string.clear_history_help), style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = { viewModel.clearHistory() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.clear_history_label))
            }
        }
    }
}
