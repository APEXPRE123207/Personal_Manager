package com.productivityapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: CategoryType, // TASK, TRANSACTION, or BOTH
    val iconName: String = "Category", // Material icon name
    val colorHex: String = "#6650a4", // Material 3 primary color by default
    val isCustom: Boolean = false // true for user-created categories
)

enum class CategoryType {
    TASK,       // For tasks only
    TRANSACTION, // For income/expenses only
    BOTH        // Can be used for both
}

// Predefined categories
object DefaultCategories {
    // Task Categories
    val WORK = Category(name = "Work", type = CategoryType.TASK, iconName = "Work", colorHex = "#1976D2")
    val PERSONAL = Category(name = "Personal", type = CategoryType.TASK, iconName = "Person", colorHex = "#7B1FA2")
    val SHOPPING = Category(name = "Shopping", type = CategoryType.BOTH, iconName = "ShoppingCart", colorHex = "#E91E63")
    val HEALTH = Category(name = "Health", type = CategoryType.BOTH, iconName = "HealthAndSafety", colorHex = "#4CAF50")
    val EDUCATION = Category(name = "Education", type = CategoryType.BOTH, iconName = "School", colorHex = "#FF9800")
    val FITNESS = Category(name = "Fitness", type = CategoryType.TASK, iconName = "FitnessCenter", colorHex = "#F44336")
    
    // Transaction Categories
    val FOOD = Category(name = "Food & Dining", type = CategoryType.TRANSACTION, iconName = "Restaurant", colorHex = "#FF5722")
    val TRANSPORT = Category(name = "Transport", type = CategoryType.TRANSACTION, iconName = "DirectionsCar", colorHex = "#3F51B5")
    val ENTERTAINMENT = Category(name = "Entertainment", type = CategoryType.TRANSACTION, iconName = "Movie", colorHex = "#9C27B0")
    val UTILITIES = Category(name = "Utilities", type = CategoryType.TRANSACTION, iconName = "ElectricalServices", colorHex = "#607D8B")
    val SALARY = Category(name = "Salary", type = CategoryType.TRANSACTION, iconName = "AccountBalance", colorHex = "#4CAF50")
    val FREELANCE = Category(name = "Freelance", type = CategoryType.TRANSACTION, iconName = "Laptop", colorHex = "#00BCD4")
    val INVESTMENT = Category(name = "Investment", type = CategoryType.TRANSACTION, iconName = "TrendingUp", colorHex = "#009688")
    val GIFT = Category(name = "Gift", type = CategoryType.TRANSACTION, iconName = "CardGiftcard", colorHex = "#E91E63")
    
    fun getAllDefaults() = listOf(
        WORK, PERSONAL, SHOPPING, HEALTH, EDUCATION, FITNESS,
        FOOD, TRANSPORT, ENTERTAINMENT, UTILITIES, SALARY, FREELANCE, INVESTMENT, GIFT
    )
    
    fun getTaskCategories() = getAllDefaults().filter { 
        it.type == CategoryType.TASK || it.type == CategoryType.BOTH 
    }
    
    fun getTransactionCategories() = getAllDefaults().filter { 
        it.type == CategoryType.TRANSACTION || it.type == CategoryType.BOTH 
    }
}
