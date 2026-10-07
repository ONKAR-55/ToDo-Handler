package com.example.todohandler.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(navController: NavController, viewModel: TaskViewModel) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedCategory by remember { mutableStateOf("Personal") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val timePickerState = rememberTimePickerState(initialHour = 7, initialMinute = 0)
    
    var durationText by remember { mutableStateOf("") }
    
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    
    val selectedDateText = datePickerState.selectedDateMillis?.let { dateFormatter.format(Date(it)) } ?: "Select Date"
    val calendar = remember(timePickerState.hour, timePickerState.minute) {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
            set(Calendar.MINUTE, timePickerState.minute)
        }
    }
    val selectedTimeText = timeFormatter.format(calendar.time)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Task", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                        val startCalendar = Calendar.getInstance().apply {
                            timeInMillis = selectedDateMillis
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                        }
                        val startTimeMillis = startCalendar.timeInMillis
                        val duration = durationText.toIntOrNull() ?: 60

                        viewModel.addTask(
                            com.example.todohandler.data.model.TaskEntity(
                                title = title,
                                description = description,
                                category = selectedCategory,
                                priority = when (selectedPriority) {
                                    "High" -> com.example.todohandler.data.model.TaskPriority.HIGH
                                    "Medium" -> com.example.todohandler.data.model.TaskPriority.MEDIUM
                                    else -> com.example.todohandler.data.model.TaskPriority.LOW
                                },
                                status = com.example.todohandler.data.model.TaskStatus.PENDING,
                                dateMillis = selectedDateMillis,
                                startTimeMillis = startTimeMillis,
                                durationMinutes = duration
                            )
                        )
                        navController.navigateUp()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Task", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Title and Description
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description & Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Section 2: Execution Window (Simplified)
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            
            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    title = { Text("Select Time") },
                    text = { TimePicker(state = timePickerState) },
                    confirmButton = {
                        TextButton(onClick = { showTimePicker = false }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                    }
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Execution Window", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = selectedDateText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Date") },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Filled.DateRange, contentDescription = "Select Date")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = selectedTimeText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Start Time") },
                            trailingIcon = {
                                IconButton(onClick = { showTimePicker = true }) {
                                    Icon(Icons.Filled.AccessTime, contentDescription = "Select Time")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("Time Window (minutes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Section 3: Priority and Category
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Priority Level", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("Low", "Medium", "High").forEach { priority ->
                            FilterChip(
                                selected = priority == selectedPriority,
                                onClick = { selectedPriority = priority },
                                label = { Text(priority) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Category", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("College", "Work", "Personal").forEach { category ->
                            FilterChip(
                                selected = category == selectedCategory,
                                onClick = { selectedCategory = category },
                                label = { Text(category) }
                            )
                        }
                    }
                }
            }

            // Section 4: Timeline Reminders
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Timeline Reminders", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    ReminderRow("10 min before start")
                    ReminderRow("On start time")
                    ReminderRow("10 min before window ends")
                    ReminderRow("On window end")
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
fun ReminderRow(text: String) {
    var checked by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text)
        Switch(checked = checked, onCheckedChange = { checked = it })
    }
}
