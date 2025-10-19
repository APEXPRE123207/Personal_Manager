package com.productivityapp

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromPriority(value: String?): Priority? {
        return value?.let { Priority.valueOf(it) }
    }

    @TypeConverter
    fun priorityToString(priority: Priority?): String? {
        return priority?.name
    }
    
    @TypeConverter
    fun fromCategoryType(value: String?): CategoryType? {
        return value?.let { CategoryType.valueOf(it) }
    }
    
    @TypeConverter
    fun categoryTypeToString(type: CategoryType?): String? {
        return type?.name
    }
    
    @TypeConverter
    fun fromTransactionType(value: String?): TransactionType? {
        return value?.let { TransactionType.valueOf(it) }
    }
    
    @TypeConverter
    fun transactionTypeToString(type: TransactionType?): String? {
        return type?.name
    }
}
