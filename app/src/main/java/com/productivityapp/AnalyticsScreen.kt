package com.productivityapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(
            (LocalContext.current.applicationContext as PersonalManagerApplication).transactionRepository
        )
    ),
    categoryViewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(
            (LocalContext.current.applicationContext as PersonalManagerApplication).categoryRepository
        )
    )
) {
    val transactions by transactionViewModel.filteredTransactions.collectAsState()
    val financialStats by transactionViewModel.financialStats.collectAsState()
    val categories by categoryViewModel.allCategories.collectAsState()
    val selectedDateRange by transactionViewModel.selectedDateRange.collectAsState()
    
    var showDateRangeMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { showDateRangeMenu = true }) {
                        Icon(Icons.Default.DateRange, "Select Period")
                    }
                    
                    DropdownMenu(
                        expanded = showDateRangeMenu,
                        onDismissRequest = { showDateRangeMenu = false }
                    ) {
                        DateRange.values().filter { it != DateRange.CUSTOM }.forEach { range ->
                            DropdownMenuItem(
                                text = { Text(range.name.replace("_", " ")) },
                                onClick = {
                                    transactionViewModel.setDateRange(range)
                                    showDateRangeMenu = false
                                }
                            )
                        }
                    }
                }
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
            // Financial Overview Card
            item {
                FinancialOverviewCard(financialStats)
            }
            
            // Income vs Expense Chart
            item {
                IncomeExpenseChart(financialStats)
            }
            
            // Income Distribution Pie Chart
            if (transactions.income().isNotEmpty()) {
                item {
                    IncomePieChart(
                        transactions = transactions.income(),
                        categories = categories
                    )
                }
            }
            
            // Expense Distribution Pie Chart
            if (transactions.expenses().isNotEmpty()) {
                item {
                    ExpensePieChart(
                        transactions = transactions.expenses(),
                        categories = categories
                    )
                }
            }
            
            // Expense Breakdown by Category
            item {
                CategoryBreakdownSection(
                    transactions = transactions.expenses(),
                    categories = categories,
                    title = "Expense Breakdown"
                )
            }
            
            // Income Breakdown by Category
            if (transactions.income().isNotEmpty()) {
                item {
                    CategoryBreakdownSection(
                        transactions = transactions.income(),
                        categories = categories,
                        title = "Income Sources"
                    )
                }
            }
            
            // Financial Insights
            item {
                FinancialInsightsCard(
                    transactions = transactions,
                    stats = financialStats
                )
            }
            
            // Top Spending Categories
            item {
                TopCategoriesCard(
                    transactions = transactions.expenses(),
                    categories = categories
                )
            }
        }
    }
}

@Composable
fun FinancialOverviewCard(stats: FinancialStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Financial Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewItem(
                    label = "Income",
                    amount = stats.totalIncome,
                    color = Color(0xFF4CAF50),
                    icon = Icons.Default.TrendingUp
                )
                OverviewItem(
                    label = "Expenses",
                    amount = stats.totalExpenses,
                    color = Color(0xFFF44336),
                    icon = Icons.Default.TrendingDown
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Net Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = NumberFormat.getCurrencyInstance().format(stats.balance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (stats.balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
                
                if (stats.totalIncome > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Savings Rate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${String.format("%.1f", stats.savingsRate)}%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewItem(
    label: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text(
            text = NumberFormat.getCurrencyInstance().format(amount),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun IncomeExpenseChart(stats: FinancialStats) {
    val modelProducer = remember { CartesianChartModelProducer.build() }
    
    LaunchedEffect(stats) {
        withContext(Dispatchers.Default) {
            modelProducer.tryRunTransaction {
                columnSeries {
                    series(stats.totalIncome, stats.totalExpenses)
                }
            }
        }
    }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Income vs Expenses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis()
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
fun ExpensePieChart(
    transactions: List<Transaction>,
    categories: List<Category>
) {
    val categoryTotals = transactions
        .groupBy { it.categoryId }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(6) // Top 6 categories
    
    if (categoryTotals.isEmpty()) return
    
    val total = categoryTotals.sumOf { it.second }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Expense Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Pie Chart Visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                PieChartCanvas(
                    categoryTotals = categoryTotals,
                    categories = categories,
                    total = total
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Legend
            categoryTotals.forEachIndexed { index, (categoryId, amount) ->
                val category = categories.find { it.id == categoryId }
                val percentage = (amount / total) * 100
                val color = getCategoryColor(category, index)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = category?.name ?: "Uncategorized",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Text(
                        text = "${String.format("%.1f", percentage)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
fun IncomePieChart(
    transactions: List<Transaction>,
    categories: List<Category>
) {
    val categoryTotals = transactions
        .groupBy { it.categoryId }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(6) // Top 6 categories
    
    if (categoryTotals.isEmpty()) return
    
    val total = categoryTotals.sumOf { it.second }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Income Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Pie Chart Visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                PieChartCanvas(
                    categoryTotals = categoryTotals,
                    categories = categories,
                    total = total
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Legend
            categoryTotals.forEachIndexed { index, (categoryId, amount) ->
                val category = categories.find { it.id == categoryId }
                val percentage = (amount / total) * 100
                val color = getCategoryColor(category, index)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = category?.name ?: "Uncategorized",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Text(
                        text = "${String.format("%.1f", percentage)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
fun PieChartCanvas(
    categoryTotals: List<Pair<Long?, Double>>,
    categories: List<Category>,
    total: Double
) {
    Canvas(
        modifier = Modifier.size(180.dp)
    ) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2f
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        
        var startAngle = -90f // Start from top
        
        categoryTotals.forEachIndexed { index, (categoryId, amount) ->
            val sweepAngle = ((amount / total) * 360f).toFloat()
            val category = categories.find { it.id == categoryId }
            val color = getCategoryColor(category, index)
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = androidx.compose.ui.geometry.Offset(
                    centerX - radius,
                    centerY - radius
                ),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
            
            startAngle += sweepAngle
        }
        
        // Draw white circle in center for donut effect
        drawCircle(
            color = androidx.compose.ui.graphics.Color.White,
            radius = radius * 0.5f,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
    }
}

fun getCategoryColor(category: Category?, index: Int): Color {
    return category?.let {
        try {
            Color(android.graphics.Color.parseColor(it.colorHex))
        } catch (e: Exception) {
            getDefaultColor(index)
        }
    } ?: getDefaultColor(index)
}

fun getDefaultColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF1976D2), Color(0xFF7B1FA2), Color(0xFFE91E63),
        Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFFF44336)
    )
    return colors[index % colors.size]
}

@Composable
fun CategoryBreakdownSection(
    transactions: List<Transaction>,
    categories: List<Category>,
    title: String
) {
    val categoryTotals = transactions
        .groupBy { it.categoryId }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
    
    if (categoryTotals.isEmpty()) return
    
    val total = categoryTotals.sumOf { it.second }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            categoryTotals.forEach { (categoryId, amount) ->
                val category = categories.find { it.id == categoryId }
                val percentage = (amount / total) * 100
                
                CategoryBreakdownItem(
                    category = category,
                    amount = amount,
                    percentage = percentage
                )
                
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun CategoryBreakdownItem(
    category: Category?,
    amount: Double,
    percentage: Double
) {
    val categoryColor = category?.let {
        try {
            Color(android.graphics.Color.parseColor(it.colorHex))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primary
        }
    } ?: MaterialTheme.colorScheme.outline
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category?.iconName ?: "Category"),
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = category?.name ?: "Uncategorized",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${String.format("%.1f", percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = NumberFormat.getCurrencyInstance().format(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = categoryColor
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { (percentage / 100).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = categoryColor,
            trackColor = categoryColor.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun FinancialInsightsCard(
    transactions: List<Transaction>,
    stats: FinancialStats
) {
    val insights = remember(transactions, stats) {
        buildList {
            if (stats.balance > 0) {
                add("Great job! You saved ${NumberFormat.getCurrencyInstance().format(stats.balance)} this period.")
            } else if (stats.balance < 0) {
                add("Warning: Expenses exceeded income by ${NumberFormat.getCurrencyInstance().format(-stats.balance)}.")
            }
            
            if (stats.savingsRate >= 20) {
                add("Excellent savings rate of ${String.format("%.1f", stats.savingsRate)}%!")
            } else if (stats.savingsRate > 0) {
                add("Consider increasing your savings rate (currently ${String.format("%.1f", stats.savingsRate)}%).")
            }
            
            if (transactions.expenses().isNotEmpty()) {
                val avgExpense = stats.totalExpenses / transactions.expenses().size
                add("Average expense: ${NumberFormat.getCurrencyInstance().format(avgExpense)}")
            }
            
            if (transactions.income().isNotEmpty()) {
                val avgIncome = stats.totalIncome / transactions.income().size
                add("Average income: ${NumberFormat.getCurrencyInstance().format(avgIncome)}")
            }
        }
    }
    
    if (insights.isEmpty()) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Financial Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            insights.forEach { insight ->
                Row {
                    Text("• ", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun TopCategoriesCard(
    transactions: List<Transaction>,
    categories: List<Category>
) {
    val topCategories = transactions
        .groupBy { it.categoryId }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(5)
    
    if (topCategories.isEmpty()) return
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Top 5 Spending Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            topCategories.forEachIndexed { index, (categoryId, amount) ->
                val category = categories.find { it.id == categoryId }
                val categoryColor = category?.let {
                    try {
                        Color(android.graphics.Color.parseColor(it.colorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                } ?: MaterialTheme.colorScheme.outline
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${index + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor,
                            modifier = Modifier.width(40.dp)
                        )
                        
                        Icon(
                            imageVector = getCategoryIcon(category?.iconName ?: "Category"),
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = category?.name ?: "Uncategorized",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    Text(
                        text = NumberFormat.getCurrencyInstance().format(amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }
                
                if (index < topCategories.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
