package com.productivityapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val deadline: Date,
    var isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val categoryId: Long? = null, // Foreign key to Category
    val createdAt: Date = Date(),
    val completedAt: Date? = null,
    val reminderTime: Date? = null, // Optional reminder
    val isRecurring: Boolean = false,
    val recurrencePattern: String? = null // "DAILY", "WEEKLY", "MONTHLY"
)

enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}
