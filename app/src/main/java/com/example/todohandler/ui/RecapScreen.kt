package com.example.todohandler.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.todohandler.data.model.TaskStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecapScreen(navController: NavController, viewModel: TaskViewModel) {
    val tasks by viewModel.allTasks.collectAsState(initial = emptyList())
    val totalCreated = tasks.size
    val completedCount = tasks.count { it.status == TaskStatus.COMPLETED }
    val missedCount = tasks.count { it.status == TaskStatus.MISSED }
    val pendingCount = tasks.count { it.status == TaskStatus.PENDING || it.status == TaskStatus.IN_PROGRESS }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Recap", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Your Summary", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Tasks Created: $totalCreated", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("✅ Completed: $completedCount", color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("❌ Missed: $missedCount", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("⏳ Pending/Active: $pendingCount")
                }
            }
        }
    }
}
