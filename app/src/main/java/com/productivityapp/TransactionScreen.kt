package com.productivityapp

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
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
    val selectedDateRange by transactionViewModel.selectedDateRange.collectAsState()
    val categories by categoryViewModel.allCategories.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf<TransactionType?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
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
                    // Filter button
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                    
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Today") },
                            onClick = {
                                transactionViewModel.setDateRange(DateRange.TODAY)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("This Week") },
                            onClick = {
                                transactionViewModel.setDateRange(DateRange.THIS_WEEK)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("This Month") },
                            onClick = {
                                transactionViewModel.setDateRange(DateRange.THIS_MONTH)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("This Year") },
                            onClick = {
                                transactionViewModel.setDateRange(DateRange.THIS_YEAR)
                                showFilterMenu = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Financial Summary Cards
            FinancialSummaryCards(financialStats)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Type Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = {
                        selectedType = null
                        transactionViewModel.setTransactionType(null)
                    },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = {
                        selectedType = TransactionType.INCOME
                        transactionViewModel.setTransactionType(TransactionType.INCOME)
                    },
                    label = { Text("Income") },
                    leadingIcon = {
                        Icon(Icons.Default.ArrowUpward, null, modifier = Modifier.size(18.dp))
                    }
                )
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = {
                        selectedType = TransactionType.EXPENSE
                        transactionViewModel.setTransactionType(TransactionType.EXPENSE)
                    },
                    label = { Text("Expenses") },
                    leadingIcon = {
                        Icon(Icons.Default.ArrowDownward, null, modifier = Modifier.size(18.dp))
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Transactions List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions yet.\nClick + to add your first transaction!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            category = categories.find { it.id == transaction.categoryId },
                            onDelete = { transactionViewModel.delete(transaction) }
                        )
                    }
                }
            }
        }
        
        if (showAddDialog) {
            AddTransactionDialog(
                categories = categories.filter { 
                    it.type == CategoryType.TRANSACTION || it.type == CategoryType.BOTH 
                },
                onDismiss = { showAddDialog = false },
                onTransactionAdd = { transaction ->
                    transactionViewModel.insert(transaction)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun FinancialSummaryCards(stats: FinancialStats) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Income Card
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Income",
                amount = stats.totalIncome,
                color = Color(0xFF4CAF50),
                icon = Icons.Default.ArrowUpward
            )
            
            // Expense Card
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Expenses",
                amount = stats.totalExpenses,
                color = Color(0xFFF44336),
                icon = Icons.Default.ArrowDownward
            )
        }
        
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
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Net Balance",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = NumberFormat.getCurrencyInstance().format(stats.balance),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (stats.balance >= 0) Color(0xFF2196F3) else Color(0xFFF44336)
                )
                if (stats.totalIncome > 0) {
                    Text(
                        text = "Savings Rate: ${String.format("%.1f", stats.savingsRate)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
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
                color = color
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    category: Category?,
    onDelete: () -> Unit
) {
    val backgroundColor = if (transaction.type == TransactionType.INCOME)
        Color(0xFF4CAF50).copy(alpha = 0.1f)
    else
        Color(0xFFF44336).copy(alpha = 0.1f)
    
    val categoryColor = category?.let {
        try {
            Color(android.graphics.Color.parseColor(it.colorHex))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primary
        }
    } ?: MaterialTheme.colorScheme.outline
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Main content row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category Icon
                category?.let {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(categoryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(it.iconName),
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                // Transaction Details
                Column(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Category name
                    category?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                color = categoryColor,
                                maxLines = 1
                            )
                        }
                    }
                    
                    // Date on separate line
                    Text(
                        text = transaction.date.toFormattedString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    
                    // Payment method
                    transaction.paymentMethod?.let {
                        Text(
                            text = "via $it",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                
                // Amount and Delete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${NumberFormat.getCurrencyInstance().format(transaction.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.type == TransactionType.INCOME) 
                            Color(0xFF4CAF50) 
                        else 
                            Color(0xFFF44336),
                        maxLines = 1
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Delete Button
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Photo section (if available) - separate row below
            transaction.photoUri?.let { uriString ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(Uri.parse(uriString)),
                            contentDescription = "Bill/Receipt",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Receipt attached",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Continues in next message due to length...

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onTransactionAdd: (Transaction) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var paymentMethod by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showCreateCategory by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoOptions by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // Create file URI for camera
    val createImageUri = remember {
        {
            val photoFile = java.io.File(
                context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES),
                "transaction_${System.currentTimeMillis()}.jpg"
            )
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) {
            photoUri = null
        }
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        photoUri = uri
    }
    
    // Permission launcher for camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createImageUri()
            photoUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(
                context,
                "Camera permission is required to take photos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    val categoryViewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory((context.applicationContext as PersonalManagerApplication).categoryRepository)
    )
    
    val typeBackgroundColor by animateColorAsState(
        targetValue = if (transactionType == TransactionType.INCOME)
            Color(0xFF4CAF50).copy(alpha = 0.1f)
        else
            Color(0xFFF44336).copy(alpha = 0.1f),
        label = "typeColor"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Add ${if (transactionType == TransactionType.INCOME) "Income" else "Expense"}"
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                // Type Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(typeBackgroundColor)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SegmentedButton(
                        selected = transactionType == TransactionType.EXPENSE,
                        onClick = { transactionType = TransactionType.EXPENSE },
                        label = "Expense",
                        icon = Icons.Default.ArrowDownward,
                        modifier = Modifier.weight(1f)
                    )
                    SegmentedButton(
                        selected = transactionType == TransactionType.INCOME,
                        onClick = { transactionType = TransactionType.INCOME },
                        label = "Income",
                        icon = Icons.Default.ArrowUpward,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("What was it for?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Text(
                            "$",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
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
                            Text(selectedCategory?.name ?: "Select Category")
                        }
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Date Selection
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Payment Method
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Payment Method (Optional)") },
                    placeholder = { Text("Cash, Card, UPI...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Photo Attachment Section
                Text(
                    text = "Attach Bill/Receipt (Optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showPhotoOptions = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (photoUri != null) "Change" else "Add Photo")
                    }
                    
                    if (photoUri != null) {
                        OutlinedButton(
                            onClick = { photoUri = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove")
                        }
                    }
                }
                
                // Show photo preview if available
                photoUri?.let { uri ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Bill/Receipt",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (description.isNotBlank() && amountValue != null && amountValue > 0) {
                        onTransactionAdd(
                            Transaction(
                                description = description,
                                amount = amountValue,
                                date = selectedDate,
                                type = transactionType,
                                categoryId = selectedCategory?.id,
                                paymentMethod = paymentMethod.ifBlank { null },
                                notes = notes.ifBlank { null },
                                photoUri = photoUri?.toString()
                            )
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Please fill in all required fields",
                            Toast.LENGTH_SHORT
                        ).show()
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
            categoryType = CategoryType.TRANSACTION,
            onDismiss = { showCreateCategory = false },
            onCategoryCreated = { newCategory ->
                categoryViewModel.insert(newCategory)
                showCreateCategory = false
            }
        )
    }
    
    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Add Photo") },
            text = {
                Column {
                    Text("Choose how to add your bill/receipt photo:")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPhotoOptions = false
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Icon(Icons.Default.Image, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gallery")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPhotoOptions = false
                        // Request camera permission
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                ) {
                    Icon(Icons.Default.CameraAlt, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Camera")
                }
            }
        )
    }
}

@Composable
fun SegmentedButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) 
                MaterialTheme.colorScheme.primary
            else
                Color.Transparent,
            contentColor = if (selected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurface
        ),
        elevation = if (selected) ButtonDefaults.buttonElevation() else null
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label)
    }
}
