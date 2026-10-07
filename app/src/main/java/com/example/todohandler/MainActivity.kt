package com.example.todohandler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.todohandler.data.local.AppDatabase
import com.example.todohandler.data.repository.TaskRepository
import com.example.todohandler.ui.AppNavigation
import com.example.todohandler.ui.TaskViewModel
import com.example.todohandler.ui.TaskViewModelFactory
import com.example.todohandler.ui.theme.ToDoHandlerTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.todohandler.ui.BottomNavigationBar
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.todohandler.ui.Screen

import com.example.todohandler.data.local.SettingsManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Database, Settings, and Repository
        val database = AppDatabase.getDatabase(this)
        val repository = TaskRepository(database.taskDao())
        val settingsManager = SettingsManager(this)
        val factory = TaskViewModelFactory(repository, settingsManager)

        enableEdgeToEdge()
        setContent {
            val taskViewModel: TaskViewModel = viewModel(factory = factory)
            val theme by taskViewModel.theme.collectAsState()

            val isDark = when (theme) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            ToDoHandlerTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                val currentScreen by navController.currentBackStackEntryAsState()

                val currentRoute = currentScreen?.destination?.route
                val showBottomBar = currentRoute in listOf(
                    Screen.Dashboard.route,
                    Screen.History.route,
                    Screen.Settings.route
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = { if (showBottomBar) BottomNavigationBar(navController) }
                ) { paddingValues ->
                    AppNavigation(
                        navController = navController,
                        taskViewModel = taskViewModel,
                        modifier = Modifier.fillMaxSize().padding(paddingValues)
                    )
                }
            }
        }
    }
}