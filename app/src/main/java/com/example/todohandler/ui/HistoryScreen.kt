package com.example.todohandler.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.todohandler.data.model.TaskStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: TaskViewModel, navController: NavHostController) {
    val allTasks by viewModel.allTasks.collectAsState(initial = emptyList())
    
    val completedCount = allTasks.count { it.status == TaskStatus.COMPLETED }
    val missedCount = allTasks.count { it.status == TaskStatus.MISSED }
    val deletedCount = allTasks.count { it.status == TaskStatus.DELETED }
    val successRate = if (completedCount + missedCount > 0) {
        (completedCount.toFloat() / (completedCount + missedCount) * 100).toInt()
    } else {
        0
    }

    val groupedTasks = allTasks.groupBy { it.dateMillis / 86400000L }.toSortedMap(reverseOrder())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Bento Metric Summary Deck
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Success Rate", fontSize = 12.sp, color = Color.Gray)
                            Text("$successRate%", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            LinearProgressIndicator(progress = { successRate / 100f },)
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Logged vs Missed", fontSize = 12.sp, color = Color.Gray)
                            Row(horizontalArrangement = Arrangement.spacedBy(50.dp)) {
                                Text("$completedCount", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("$missedCount", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            
            // Calendar Navigator Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {}) { Text("This Week") }
                    Button(onClick = {}) { Text("Month") }
                }
            }

            // Filter Carousel
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    FilterChip(selected = true, onClick = {}, label = { Text("All (${allTasks.size})") })
                    FilterChip(selected = false, onClick = {}, label = { Text("Completed ($completedCount)") })
                    FilterChip(selected = false, onClick = {}, label = { Text("Missed ($missedCount)") })
                    FilterChip(selected = false, onClick = {}, label = {Text("Deleted($deletedCount)")})
                }
            }

            if (groupedTasks.isEmpty()) {
                item {
                    Text("No task history yet.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                }
            }

            // Historical Timeline Feed
            groupedTasks.forEach { (dayIndex, dayTasks) ->
                item {
                    Text("Day Index: $dayIndex", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                }
                items(dayTasks) { task ->
                    val isSuccess = task.status == TaskStatus.COMPLETED
                    val isMissed = task.status == TaskStatus.MISSED
                    HistoryTaskCard(title = task.title, time = task.status.name, success = isSuccess, error = isMissed)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun HistoryTaskCard(title: String, time: String, success: Boolean, error: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(time, fontSize = 12.sp, color = Color.Gray)
            }
            if (success) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else if (error) {
                Icon(
                    Icons.Filled.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
