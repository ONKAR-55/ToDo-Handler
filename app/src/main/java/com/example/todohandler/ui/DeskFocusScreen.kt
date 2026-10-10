package com.example.todohandler.ui


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.todohandler.data.model.TaskStatus
import com.example.todohandler.domain.TaskLogic
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DeskFocusScreen(navController: NavController, viewModel: TaskViewModel) {
    val selectedTask by viewModel.selectedTask.collectAsState()
    var isRunning by remember { mutableStateOf(true) }
    
    var remainingSeconds by remember(selectedTask) {
        mutableIntStateOf(
            selectedTask?.let { task ->
                TaskLogic.calculateRemainingSeconds(task)
            } ?: (60 * 60)
        ) 
    }

    LaunchedEffect(isRunning) {
        while (isRunning && remainingSeconds > 0) {
            delay(1000.milliseconds)
            remainingSeconds--
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF000000), // Deep dark for focus mode
    ) {
        Column(
            modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = selectedTask?.title ?: "No Task Selected",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = selectedTask?.category ?: "Unknown Category",
                color = Color.Gray,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Timer Display
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                color = Color.Gray,
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add Time Button
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .size(64.dp)
                        .clickable {
                            selectedTask?.let { task ->
                                val newDuration = task.durationMinutes + 15
                                val updatedTask = task.copy(durationMinutes = newDuration)
                                viewModel.updateTask(updatedTask)
                                viewModel.selectTask(updatedTask)
                                remainingSeconds += 15 * 60
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+15m", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                
                // Play/Pause Button
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    contentColor = Color.White,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Toggle Timer",
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                // End Button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF059669).copy(alpha = 0.2f),
                    modifier = Modifier
                        .size(64.dp)
                        .clickable {
                            selectedTask?.let { task ->
                                viewModel.updateTaskStatus(task, TaskStatus.COMPLETED)
                            }
                            navController.navigateUp()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Done", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Exit Button
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Filled.Close, contentDescription = "Exit Desk Mode", tint = Color.Gray)
            }
        }
    }
}
