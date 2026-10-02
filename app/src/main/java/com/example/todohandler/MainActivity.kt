package com.example.todohandler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Database and Repository
        val database = AppDatabase.getDatabase(this)
        val repository = TaskRepository(database.taskDao())
        val factory = TaskViewModelFactory(repository)

        enableEdgeToEdge()
        setContent {
            ToDoHandlerTheme {
                val navController = rememberNavController()
                val taskViewModel: TaskViewModel = viewModel(factory = factory)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        taskViewModel = taskViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}