package com.example.fish_tasks_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.fish_tasks_android.ui.FishApp
import com.example.fish_tasks_android.ui.theme.Fish_tasks_androidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Fish_tasks_androidTheme {
                FishApp()
            }
        }
    }
}