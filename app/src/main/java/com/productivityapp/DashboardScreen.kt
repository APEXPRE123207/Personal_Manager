package com.productivityapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    isDarkMode: Boolean = false,
    onToggleTheme: () -> Unit = {},
    taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(
            (LocalContext.current.applicationContext as PersonalManagerApplication).taskRepository
        )
    ),
    transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(
            (LocalContext.current.applicationContext as PersonalManagerApplication).transactionRepository
        )
    ),
    noteViewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(
            (LocalContext.current.applicationContext as PersonalManagerApplication).noteRepository
        )
    ),
    categoryViewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(
            (LocalContext.current.applicationContext as PersonalManagerApplication).categoryRepository
        )
    )
) {
    val tasks by taskViewModel.allTasks.collectAsState()
    val transactions by transactionViewModel.allTransactions.collectAsState()
    val financialStats by transactionViewModel.financialStats.collectAsState()
    val notes by noteViewModel.allNotes.collectAsState()
    val categories by categoryViewModel.allCategories.collectAsState()
    
    // Calculate task statistics
    val pendingTasks = tasks.filter { !it.isCompleted }
    val highPriorityTasks = pendingTasks.filter { it.priority == Priority.HIGH }
    val completionRate = if (tasks.isNotEmpty()) {
        (tasks.count { it.isCompleted }.toFloat() / tasks.size) * 100
    } else 0f
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("FLOW Dashboard")
                        Text(
                            text = "Your life, organized",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Financial Overview Section
            item {
                DashboardSection(
                    title = "Financial Overview",
                    icon = Icons.Default.AccountBalance,
                    onViewAll = { navController.navigate("transactions") }
                ) {
                    FinancialOverviewDashboard(financialStats)
                }
            }
            
            // Recent Transactions
            item {
                DashboardSection(
                    title = "Recent Transactions",
                    icon = Icons.Default.Receipt,
                    onViewAll = { navController.navigate("transactions") }
                ) {
                    RecentTransactionsDashboard(
                        transactions = transactions.take(3),
                        categories = categories,
                        onTransactionClick = { navController.navigate("transactions") }
                    )
                }
            }
            
            // Tasks Section
            item {
                DashboardSection(
                    title = "Tasks",
                    icon = Icons.Default.CheckCircle,
                    onViewAll = { navController.navigate("tasks") }
                ) {
                    TasksDashboard(
                        tasks = pendingTasks.take(4),
                        categories = categories,
                        totalPending = pendingTasks.size,
                        highPriority = highPriorityTasks.size,
                        completionRate = completionRate,
                        onTaskClick = { navController.navigate("tasks") }
                    )
                }
            }
            
            // Notes Section
            item {
                DashboardSection(
                    title = "Recent Notes",
                    icon = Icons.Default.Create,
                    onViewAll = { navController.navigate("notes") }
                ) {
                    NotesDashboard(
                        notes = notes.take(3),
                        totalNotes = notes.size,
                        lockedNotes = notes.count { it.password != null },
                        onNoteClick = { navController.navigate("notes") }
                    )
                }
            }
            
            // Quick Actions
            item {
                QuickActionsSection(navController)
            }
        }
    }
}

@Composable
fun DashboardSection(
    title: String,
    icon: ImageVector,
    onViewAll: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(onClick = onViewAll) {
                    Text("View All")
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            content()
        }
    }
}

@Composable
fun FinancialOverviewDashboard(stats: FinancialStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Income Card
        FinancialMiniCard(
            modifier = Modifier.weight(1f),
            title = "Income",
            amount = stats.totalIncome,
            color = Color(0xFF4CAF50),
            icon = Icons.Default.TrendingUp
        )
        
        // Expense Card
        FinancialMiniCard(
            modifier = Modifier.weight(1f),
            title = "Expenses",
            amount = stats.totalExpenses,
            color = Color(0xFFF44336),
            icon = Icons.Default.TrendingDown
        )
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    // Balance Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (stats.balance >= 0) 
                Color(0xFF2196F3).copy(alpha = 0.1f)
            else 
                Color(0xFFF44336).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Net Balance",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = NumberFormat.getCurrencyInstance().format(stats.balance),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (stats.balance >= 0) Color(0xFF2196F3) else Color(0xFFF44336)
                )
            }
            
            if (stats.totalIncome > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Savings",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.1f", stats.savingsRate)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                }
            }
        }
    }
}

@Composable
fun FinancialMiniCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    color: Color,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = NumberFormat.getCurrencyInstance().format(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RecentTransactionsDashboard(
    transactions: List<Transaction>,
    categories: List<Category>,
    onTransactionClick: () -> Unit
) {
    if (transactions.isEmpty()) {
        Text(
            text = "No transactions yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            transactions.forEach { transaction ->
                TransactionDashboardItem(
                    transaction = transaction,
                    category = categories.find { it.id == transaction.categoryId },
                    onClick = onTransactionClick
                )
            }
        }
    }
}

@Composable
fun TransactionDashboardItem(
    transaction: Transaction,
    category: Category?,
    onClick: () -> Unit
) {
    val categoryColor = category?.let {
        try {
            Color(android.graphics.Color.parseColor(it.colorHex))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primary
        }
    } ?: MaterialTheme.colorScheme.outline
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.type == TransactionType.INCOME)
                Color(0xFF4CAF50).copy(alpha = 0.05f)
            else
                Color(0xFFF44336).copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category?.iconName ?: "Category"),
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = transaction.date.toFormattedString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"} ${NumberFormat.getCurrencyInstance().format(transaction.amount)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.INCOME) 
                    Color(0xFF4CAF50) 
                else 
                    Color(0xFFF44336)
            )
        }
    }
}

@Composable
fun TasksDashboard(
    tasks: List<Task>,
    categories: List<Category>,
    totalPending: Int,
    highPriority: Int,
    completionRate: Float,
    onTaskClick: () -> Unit
) {
    // Statistics Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskStatCard(
            modifier = Modifier.weight(1f),
            label = "Pending",
            value = totalPending.toString(),
            icon = Icons.Default.Schedule,
            color = MaterialTheme.colorScheme.primary
        )
        TaskStatCard(
            modifier = Modifier.weight(1f),
            label = "High Priority",
            value = highPriority.toString(),
            icon = Icons.Default.PriorityHigh,
            color = MaterialTheme.colorScheme.error
        )
        TaskStatCard(
            modifier = Modifier.weight(1f),
            label = "Done",
            value = "${completionRate.toInt()}%",
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF4CAF50)
        )
    }
    
    if (tasks.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tasks.forEach { task ->
                TaskDashboardItem(
                    task = task,
                    category = categories.find { it.id == task.categoryId },
                    onClick = onTaskClick
                )
            }
        }
    }
}

@Composable
fun TaskStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TaskDashboardItem(
    task: Task,
    category: Category?,
    onClick: () -> Unit
) {
    val categoryColor = category?.let {
        try {
            Color(android.graphics.Color.parseColor(it.colorHex))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primary
        }
    } ?: MaterialTheme.colorScheme.outline
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (task.priority) {
                            Priority.HIGH -> MaterialTheme.colorScheme.error
                            Priority.MEDIUM -> MaterialTheme.colorScheme.primary
                            Priority.LOW -> MaterialTheme.colorScheme.secondary
                        }
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    category?.let {
                        Icon(
                            imageVector = getCategoryIcon(it.iconName),
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor
                        )
                        Text(" • ", style = MaterialTheme.typography.labelSmall)
                    }
                    Text(
                        text = task.deadline.toFormattedString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun NotesDashboard(
    notes: List<Note>,
    totalNotes: Int,
    lockedNotes: Int,
    onNoteClick: () -> Unit
) {
    // Statistics Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = totalNotes.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total Notes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = lockedNotes.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Locked",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    if (notes.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            notes.forEach { note ->
                NoteDashboardItem(note = note, onClick = onNoteClick)
            }
        }
    }
}

@Composable
fun NoteDashboardItem(
    note: Note,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.StickyNote2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.body.isNotBlank() && note.password == null) {
                    Text(
                        text = note.body.take(40) + if (note.body.length > 40) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            if (note.password != null) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun QuickActionsSection(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    QuickActionButton(
                        icon = Icons.Default.CheckCircle,
                        label = "Tasks",
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { navController.navigate("tasks") }
                    )
                }
                item {
                    QuickActionButton(
                        icon = Icons.Default.AccountBalance,
                        label = "Money",
                        color = Color(0xFF2196F3),
                        onClick = { navController.navigate("transactions") }
                    )
                }
                item {
                    QuickActionButton(
                        icon = Icons.Default.Analytics,
                        label = "Analytics",
                        color = Color(0xFF9C27B0),
                        onClick = { navController.navigate("analytics") }
                    )
                }
                item {
                    QuickActionButton(
                        icon = Icons.Default.Create,
                        label = "Notes",
                        color = Color(0xFFFF9800),
                        onClick = { navController.navigate("notes") }
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}
