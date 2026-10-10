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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.todohandler.data.model.TaskStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: TaskViewModel, navController: NavHostController) {
    val allTasks by viewModel.allTasks.collectAsState(initial = emptyList())
    
    var selectedFilter by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("All") }
    
    val completedCount = allTasks.count { it.status == TaskStatus.COMPLETED }
    val missedCount = allTasks.count { it.status == TaskStatus.MISSED }
    val deletedCount = allTasks.count { it.status == TaskStatus.DELETED }
    val successRate = if (completedCount + missedCount > 0) {
        (completedCount.toFloat() / (completedCount + missedCount) * 100).toInt()
    } else {
        0
    }

    val filteredTasks = when (selectedFilter) {
        "Completed" -> allTasks.filter { it.status == TaskStatus.COMPLETED }
        "Missed" -> allTasks.filter { it.status == TaskStatus.MISSED }
        "Deleted" -> allTasks.filter { it.status == TaskStatus.DELETED }
        else -> allTasks
    }

    val groupedTasks = filteredTasks.groupBy { it.dateMillis / 86400000L }.toSortedMap(reverseOrder())

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
                            LinearProgressIndicator(progress = { successRate / 100f })
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Completed vs Missed", fontSize = 12.sp, color = Color.Gray)
                            Row(horizontalArrangement = Arrangement.spacedBy(70.dp)) {
                                Text("$completedCount", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("$missedCount", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Filter Carousel
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Row(horizontalArrangement = Arrangement.spacedBy(90.dp) ) {
                        Column (horizontalAlignment = Alignment.CenterHorizontally) {
                            FilterChip(modifier = Modifier.width(150.dp), selected = selectedFilter == "All", onClick = { selectedFilter = "All" }, label = { Text("All  (${allTasks.size})", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontSize = 17.sp) })
                            FilterChip(modifier = Modifier.width(150.dp), selected = selectedFilter == "Completed", onClick = { selectedFilter = "Completed" }, label = { Text("Completed  ($completedCount)", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 17.sp, modifier = Modifier.fillMaxWidth()) })
                        }
                        Column (horizontalAlignment = Alignment.CenterHorizontally) {
                            FilterChip(modifier = Modifier.width(150.dp), selected = selectedFilter == "Missed", onClick = { selectedFilter = "Missed" }, label = { Text("Missed  ($missedCount)", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 17.sp, modifier = Modifier.fillMaxWidth()) })
                            FilterChip(modifier = Modifier.width(150.dp), selected = selectedFilter == "Deleted", onClick = { selectedFilter = "Deleted" }, label = {Text("Deleted  ($deletedCount)", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 17.sp, modifier = Modifier.fillMaxWidth())})
                        }
                    }
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
                    val date = LocalDate.ofEpochDay(dayIndex.toLong())
                    val formatDate = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))
                    Text("Day : $formatDate", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
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
