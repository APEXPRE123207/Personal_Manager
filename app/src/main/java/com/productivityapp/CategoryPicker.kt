package com.productivityapp

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun CategoryPickerDialog(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    onDismiss: () -> Unit,
    onCreateNew: () -> Unit
) {
    // Remove duplicates based on category name and type
    val uniqueCategories = categories.distinctBy { "${it.name}-${it.type}" }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(uniqueCategories) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = category.id == selectedCategory?.id,
                            onClick = {
                                onCategorySelected(category)
                                onDismiss()
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    TextButton(onClick = {
                        onCreateNew()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Category")
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primaryContainer
    }
    
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) backgroundColor.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) backgroundColor else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(backgroundColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getCategoryIcon(category.iconName),
                contentDescription = category.name,
                tint = backgroundColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            color = if (isSelected) backgroundColor else MaterialTheme.colorScheme.onSurface
        )
    }
}

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "Work" -> Icons.Default.Work
        "Person", "Personal" -> Icons.Default.Person
        "ShoppingCart", "Shopping" -> Icons.Default.ShoppingCart
        "HealthAndSafety", "Health" -> Icons.Default.HealthAndSafety
        "School", "Education" -> Icons.Default.School
        "FitnessCenter", "Fitness" -> Icons.Default.FitnessCenter
        "Restaurant", "Food" -> Icons.Default.Restaurant
        "DirectionsCar", "Transport" -> Icons.Default.DirectionsCar
        "Movie", "Entertainment" -> Icons.Default.Movie
        "ElectricalServices", "Utilities" -> Icons.Default.ElectricalServices
        "AccountBalance", "Salary" -> Icons.Default.AccountBalance
        "Laptop", "Freelance" -> Icons.Default.Laptop
        "TrendingUp", "Investment" -> Icons.Default.TrendingUp
        "CardGiftcard", "Gift" -> Icons.Default.CardGiftcard
        else -> Icons.Default.Category
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCategoryDialog(
    categoryType: CategoryType,
    onDismiss: () -> Unit,
    onCategoryCreated: (Category) -> Unit
) {
    val context = LocalContext.current
    var categoryName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("Category") }
    var selectedColor by remember { mutableStateOf("#6650a4") }
    
    val availableIcons = listOf(
        "Work", "Person", "ShoppingCart", "HealthAndSafety", "School",
        "FitnessCenter", "Restaurant", "DirectionsCar", "Movie",
        "ElectricalServices", "AccountBalance", "Laptop", "TrendingUp", "CardGiftcard"
    )
    
    val availableColors = listOf(
        "#1976D2", "#7B1FA2", "#E91E63", "#4CAF50", "#FF9800",
        "#F44336", "#FF5722", "#3F51B5", "#9C27B0", "#607D8B",
        "#00BCD4", "#009688", "#CDDC39", "#FFC107"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g., Groceries") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Select Icon", style = MaterialTheme.typography.labelMedium)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(120.dp)
                ) {
                    items(availableIcons.size) { index ->
                        val iconName = availableIcons[index]
                        IconButton(
                            onClick = { selectedIcon = iconName },
                            modifier = Modifier
                                .size(40.dp)
                                .border(
                                    width = if (selectedIcon == iconName) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(iconName),
                                contentDescription = iconName,
                                tint = if (selectedIcon == iconName) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Select Color", style = MaterialTheme.typography.labelMedium)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableColors.take(7).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .border(
                                    width = if (selectedColor == color) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        val newCategory = Category(
                            name = categoryName,
                            type = categoryType,
                            iconName = selectedIcon,
                            colorHex = selectedColor,
                            isCustom = true
                        )
                        onCategoryCreated(newCategory)
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Please enter a category name", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
