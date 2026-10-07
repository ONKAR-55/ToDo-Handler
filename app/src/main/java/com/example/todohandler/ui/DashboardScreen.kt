package com.example.todohandler.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.todohandler.data.model.TaskEntity
import com.example.todohandler.data.model.TaskPriority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.todohandler.domain.TaskLogic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: TaskViewModel) {
    val tasks by viewModel.tasksForSelectedDate.collectAsState()
    val completedTasks by viewModel.completedTasksForSelectedDate.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val streakDays by viewModel.streakDays.collectAsState()

    val filteredTasks = if (selectedCategory == "All") tasks else tasks.filter { it.category == selectedCategory }
    val filteredCompletedTasks = if (selectedCategory == "All") completedTasks else completedTasks.filter { it.category == selectedCategory }
    
    val filteredInProgress = filteredTasks.count { it.status == com.example.todohandler.data.model.TaskStatus.IN_PROGRESS }
    val filteredUpcoming = filteredTasks.count { it.status == com.example.todohandler.data.model.TaskStatus.PENDING }
    val filteredMissed = filteredTasks.count { it.status == com.example.todohandler.data.model.TaskStatus.MISSED }

    val selectedDateMillis by viewModel.selectedDateMillis.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { viewModel.selectDate(it) }
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ToDo Handler", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddTask.route) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, "Add Task")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateString = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(selectedDateMillis))
                Text(
                    text = dateString,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showDatePicker = true }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.navigate(Screen.Recap.route) }) {
                        Icon(Icons.Filled.Assessment, contentDescription = "Recap")
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            "$streakDays Day Streak",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            MetricsRow(filteredInProgress, filteredUpcoming, filteredMissed)

            Spacer(modifier = Modifier.height(16.dp))
            CategoryFilter(selectedCategory, onCategorySelect = { viewModel.setCategory(it) })

            Spacer(modifier = Modifier.height(24.dp))
            Text("Active Right Now", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            val activeTask = filteredTasks.find { it.status == com.example.todohandler.data.model.TaskStatus.IN_PROGRESS }
            val pendingTasks = filteredTasks.filter { it.status == com.example.todohandler.data.model.TaskStatus.PENDING }

            if (activeTask != null) {
                ActiveTaskCard(
                    task = activeTask,
                    onDeskClick = { navController.navigate(Screen.DeskFocus.route) },
                    onClick = {
                        viewModel.selectTask(activeTask)
                        navController.navigate(Screen.TaskDetail.route)
                    }
                )
            } else {
                Text("No active task right now", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Pending Tasks", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (pendingTasks.isNotEmpty()) {
                pendingTasks.forEach { task ->
                    PendingTaskCard(title = task.title, time = "Due Today", onClick = {
                        viewModel.selectTask(task)
                        navController.navigate(Screen.TaskDetail.route)
                    })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Text("No pending tasks", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Completed Tasks", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (filteredCompletedTasks.isNotEmpty()) {
                filteredCompletedTasks.forEach { task ->
                    PendingTaskCard(title = task.title, time = "Completed", onClick = {
                        viewModel.selectTask(task)
                        navController.navigate(Screen.TaskDetail.route)
                    })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Text("No completed tasks", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MetricsRow(inProgress: Int, upcoming: Int, missed: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricItem("In Progress", inProgress.toString(), Color(0xFF2563EB), Modifier.weight(1f))
        MetricItem("Upcoming", upcoming.toString(), Color(0xFF4F46E5), Modifier.weight(1f))
        MetricItem("Missed", missed.toString(), Color(0xFFDC2626), Modifier.weight(1f))
    }
}

@Composable
fun MetricItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun CategoryFilter(selectedCategory: String, onCategorySelect: (String) -> Unit) {
    val categories = listOf("All", "College", "Personal", "Work")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            Surface(
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { onCategorySelect(category) }
            ) {
                Text(
                    text = category,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ActiveTaskCard(task: TaskEntity, onDeskClick: () -> Unit, onClick: () -> Unit) {
    val isHighPriority = task.priority == TaskPriority.HIGH
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isHighPriority) {
                        Surface(shape = CircleShape, color = Color(0xFFFEE2E2)) {
                            Text("High Priority", color = Color(0xFFB91C1C), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(task.category, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                Button(
                    onClick = onDeskClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Desk Mode", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(task.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            
            val dateString = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(task.dateMillis))
            Text("$dateString • ${task.category}", fontSize = 12.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val startTimeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(task.startTimeMillis))
                    val endTimeMillis = task.startTimeMillis + task.durationMinutes * 60 * 1000L
                    val endTimeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(endTimeMillis))
                    
                    val remainingMinutes = TaskLogic.calculateRemainingMinutes(task)
                    val progress = TaskLogic.calculateTaskProgress(task)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$startTimeStr - $endTimeStr (${task.durationMinutes}m window)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("${remainingMinutes}m remaining", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun PendingTaskCard(title: String, time: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(time, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
