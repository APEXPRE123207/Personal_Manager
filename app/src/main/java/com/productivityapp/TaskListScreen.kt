package com.productivityapp

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController: NavController,
    taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory((LocalContext.current.applicationContext as PersonalManagerApplication).taskRepository)
    ),
    categoryViewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory((LocalContext.current.applicationContext as PersonalManagerApplication).categoryRepository)
    )
) {
    val tasks by taskViewModel.allTasks.collectAsState()
    val categories by categoryViewModel.allCategories.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPriorityFilter by remember { mutableStateOf<Priority?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<Category?>(null) }
    var showCompletedTasks by remember { mutableStateOf(true) }

    // Filter tasks
    val filteredTasks = remember(tasks, searchQuery, selectedPriorityFilter, selectedCategoryFilter, showCompletedTasks) {
        tasks.filter { task ->
            val matchesSearch = if (searchQuery.isBlank()) true else 
                task.title.contains(searchQuery, ignoreCase = true)
            val matchesPriority = selectedPriorityFilter?.let { it == task.priority } ?: true
            val matchesCategory = selectedCategoryFilter?.let { it.id == task.categoryId } ?: true
            val matchesCompleted = showCompletedTasks || !task.isCompleted
            matchesSearch && matchesPriority && matchesCategory && matchesCompleted
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search tasks...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            // Filter chips - Priority
            Text(
                text = "Priority",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedPriorityFilter == null,
                    onClick = { selectedPriorityFilter = null },
                    label = { Text("All") }
                )
                Priority.values().forEach { priority ->
                    FilterChip(
                        selected = selectedPriorityFilter == priority,
                        onClick = { 
                            selectedPriorityFilter = if (selectedPriorityFilter == priority) null else priority 
                        },
                        label = { Text(priority.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (priority) {
                                Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
                                Priority.MEDIUM -> MaterialTheme.colorScheme.primaryContainer
                                Priority.LOW -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        )
                    )
                }
            }

            // Filter chips - Category
            val taskCategories = categories.filter { it.type == CategoryType.TASK || it.type == CategoryType.BOTH }
            if (taskCategories.isNotEmpty()) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All") }
                        )
                    }
                    items(taskCategories) { category ->
                        FilterChip(
                            selected = selectedCategoryFilter?.id == category.id,
                            onClick = { 
                                selectedCategoryFilter = if (selectedCategoryFilter?.id == category.id) null else category 
                            },
                            label = { Text(category.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getCategoryIcon(category.iconName),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Show completed toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = showCompletedTasks,
                    onClick = { showCompletedTasks = !showCompletedTasks },
                    label = { Text("Show Completed (${tasks.count { it.isCompleted }})") }
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredTasks.isEmpty()) {
                    item {
                        Text(
                            text = if (tasks.isEmpty()) {
                                "No tasks yet. Click + to add your first task!"
                            } else {
                                "No tasks match your filters or search."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(filteredTasks) { task ->
                    TaskItem(
                        task = task,
                        category = categories.find { it.id == task.categoryId },
                        onUpdate = { taskViewModel.update(it) },
                        onDelete = { taskViewModel.delete(it) }
                    )
                    }
                }
            }
        }

        if (showDialog) {
            AddTaskDialog(
                categories = categories.filter { it.type == CategoryType.TASK || it.type == CategoryType.BOTH },
                onDismiss = { showDialog = false },
                onTaskAdd = { task ->
                    taskViewModel.insert(task)
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onTaskAdd: (Task) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var expanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showCreateCategory by remember { mutableStateOf(false) }
    
    val categoryViewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory((context.applicationContext as PersonalManagerApplication).categoryRepository)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = { Text("Enter task description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Category Selection
                OutlinedButton(
                    onClick = { showCategoryPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            selectedCategory?.let {
                                Icon(
                                    imageVector = getCategoryIcon(it.iconName),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(selectedCategory?.name ?: "Select Category (Optional)")
                        }
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Priority Selection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = priority.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        Priority.values().forEach { priorityValue ->
                            DropdownMenuItem(
                                text = { Text(priorityValue.name) },
                                onClick = {
                                    priority = priorityValue
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Deadline Selection
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(selectedDate.toFormattedString())
                        }
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onTaskAdd(
                            Task(
                                title = title,
                                priority = priority,
                                deadline = selectedDate,
                                categoryId = selectedCategory?.id
                            )
                        )
                    } else {
                        Toast.makeText(context, "Please enter a task title", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDate = it },
            onDismiss = { showDatePicker = false },
            initialDate = selectedDate
        )
    }
    
    if (showCategoryPicker) {
        CategoryPickerDialog(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            onDismiss = { showCategoryPicker = false },
            onCreateNew = { showCreateCategory = true }
        )
    }
    
    if (showCreateCategory) {
        CreateCategoryDialog(
            categoryType = CategoryType.TASK,
            onDismiss = { showCreateCategory = false },
            onCategoryCreated = { newCategory ->
                categoryViewModel.insert(newCategory)
                showCreateCategory = false
            }
        )
    }
}


@Composable
fun TaskItem(
    task: Task,
    category: Category?,
    onUpdate: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    val categoryColor = category?.let {
        try {
            androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(it.colorHex))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primary
        }
    } ?: MaterialTheme.colorScheme.outline
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { isChecked -> onUpdate(task.copy(isCompleted = isChecked)) }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (task.isCompleted) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    category?.let {
                        Icon(
                            imageVector = getCategoryIcon(it.iconName),
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = categoryColor
                        )
                        Text(" • ", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = "Priority: ${task.priority}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when (task.priority) {
                            Priority.HIGH -> MaterialTheme.colorScheme.error
                            Priority.MEDIUM -> MaterialTheme.colorScheme.primary
                            Priority.LOW -> MaterialTheme.colorScheme.secondary
                        }
                    )
                }
                Text(
                    text = "Deadline: ${task.deadline.toFormattedString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onDelete(task) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskListScreenPreview() {
    TaskItem(
        task = Task(title = "Sample Task", deadline = Date(), priority = Priority.HIGH),
        category = null,
        onUpdate = {},
        onDelete = {}
    )
}
