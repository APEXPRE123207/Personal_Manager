package com.productivityapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val amount: Double,
    val date: Date,
    val type: TransactionType, // INCOME or EXPENSE
    val categoryId: Long? = null, // Foreign key to Category
    val paymentMethod: String? = null, // "Cash", "Card", "UPI", etc.
    val isRecurring: Boolean = false,
    val recurrencePattern: String? = null, // \"DAILY\", \"WEEKLY\", \"MONTHLY\"
    val tags: String? = null, // Comma-separated tags for additional filtering
    val notes: String? = null, // Additional notes
    val photoUri: String? = null // URI to bill/receipt photo
)

enum class TransactionType {
    INCOME,
    EXPENSE
}

// Extension functions for easy filtering
fun List<Transaction>.filterByType(type: TransactionType) = filter { it.type == type }
fun List<Transaction>.income() = filterByType(TransactionType.INCOME)
fun List<Transaction>.expenses() = filterByType(TransactionType.EXPENSE)
fun List<Transaction>.totalIncome() = income().sumOf { it.amount }
fun List<Transaction>.totalExpenses() = expenses().sumOf { it.amount }
fun List<Transaction>.balance() = totalIncome() - totalExpenses()
