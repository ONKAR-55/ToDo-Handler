package com.example.todohandler.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: TaskViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    isExporting = true
                    val (count, csvData) = viewModel.exportTasksLast30DaysCsv()
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(csvData.toByteArray(Charsets.UTF_8))
                        outputStream.flush()
                    }
                    withContext(Dispatchers.Main) {
                        isExporting = false
                        Toast.makeText(
                            context,
                            "Exported $count task(s) successfully!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isExporting = false
                        Toast.makeText(
                            context,
                            "Failed to export: ${e.localizedMessage ?: "Unknown error"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance & Theme
            item {
                Text("Appearance & Theme", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Interface Theme", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val selectedTheme by viewModel.theme.collectAsState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("System", "Light", "Dark").forEach { theme ->
                            val isSelected = selectedTheme == theme
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setTheme(theme) }
                            ) {
                                Text(
                                    text = theme,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
            
            // Time Window Rules
            item {
                val windowStartNotification by viewModel.windowStartNotification.collectAsState()
                val preEndWrapWarning by viewModel.preEndWrapWarning.collectAsState()

                Text("Time Window Rules", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                ListItem(
                    headlineContent = { Text("Window Start Notification") },
                    supportingContent = { Text("Haptic buzz & dedicated chime at start") },
                    trailingContent = { Switch(checked = windowStartNotification, onCheckedChange = { viewModel.setWindowStartNotification(it) }) }
                )
                ListItem(
                    headlineContent = { Text("Pre-End Wrap Warning") },
                    supportingContent = { Text("Gently nudge 10m before target completion") },
                    trailingContent = { Switch(checked = preEndWrapWarning, onCheckedChange = { viewModel.setPreEndWrapWarning(it) }) }
                )
                HorizontalDivider()
            }
            
            // Task & Schedule Defaults
            item {
                Text("Task & Schedule Defaults", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                ListItem(
                    headlineContent = { Text("Default Focus Block") },
                    supportingContent = { Text("60m") }
                )
                HorizontalDivider()
            }

            // Data & Local Storage
            item {
                Text("Data & Portability", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                ListItem(
                    headlineContent = { Text("Export Tasks (Last 30 Days)") },
                    supportingContent = { Text("Save tasks created or scheduled in the last 30 days as CSV") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export CSV",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            FilledTonalButton(
                                onClick = {
                                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                    exportLauncher.launch("tasks_export_$timeStamp.csv")
                                }
                            ) {
                                Text("Export")
                            }
                        }
                    },
                    modifier = Modifier.clickable(enabled = !isExporting) {
                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        exportLauncher.launch("tasks_export_$timeStamp.csv")
                    }
                )
                HorizontalDivider()
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
