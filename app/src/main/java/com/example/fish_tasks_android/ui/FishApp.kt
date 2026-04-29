package com.example.fish_tasks_android.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.fish_tasks_android.FishViewModel
import com.example.fish_tasks_android.R
import com.example.fish_tasks_android.ui.screens.*

sealed class Screen(val route: String, val titleRes: Int, val iconRes: Int) {
    object Fish : Screen("fish", R.string.go_fish, R.drawable.blue_fish_profile)
    object Stats : Screen("stats", R.string.stats, R.drawable.fisherman)
    object History : Screen("history", R.string.history, R.drawable.back_arrow)
}

@Composable
fun FishApp(
    viewModel: FishViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                val screens = listOf(Screen.Fish, Screen.Stats, Screen.History)
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                painter = painterResource(screen.iconRes), 
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            ) 
                        },
                        label = { Text(stringResource(screen.titleRes)) },
                        selected = currentDestination?.route == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Fish.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Fish.route) {
                MainFishScreen(viewModel, navController)
            }
            composable(Screen.Stats.route) {
                StatsScreen(viewModel)
            }
            composable(Screen.History.route) {
                HistoryScreen(viewModel)
            }
            composable("add") {
                AddTaskScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable("settings") {
                SettingsScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable("edit/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")
                EditTaskScreen(viewModel, taskId, onBack = { navController.popBackStack() })
            }
        }
    }
}
