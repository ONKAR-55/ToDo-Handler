package com.example.todohandler.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AddTask : Screen("add_task")
    object History : Screen("history")
    object Settings : Screen("settings")
    object DeskFocus : Screen("desk_focus")
    object TaskDetail : Screen("task_detail")
    object Recap : Screen("recap")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    taskViewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    // Smooth standard iOS-like push/pop transitions for all screens by default
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 4 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController, viewModel = taskViewModel)
        }
        composable(Screen.AddTask.route) {
            AddTaskScreen(navController = navController, viewModel = taskViewModel)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController, viewModel = taskViewModel)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController, viewModel = taskViewModel)
        }
        composable(
            route = Screen.DeskFocus.route,
            // Desk focus is immersive, so a slow fade in/out feels more focused than sliding
            enterTransition = { fadeIn(tween(500)) },
            exitTransition = { fadeOut(tween(500)) },
            popEnterTransition = { fadeIn(tween(500)) },
            popExitTransition = { fadeOut(tween(500)) }
        ) {
            DeskFocusScreen(navController = navController, viewModel = taskViewModel)
        }
        composable(Screen.TaskDetail.route) {
            TaskDetailScreen(navController = navController, viewModel = taskViewModel)
        }
        composable(Screen.Recap.route) {
            RecapScreen(navController = navController, viewModel = taskViewModel)
        }
    }
}
