package com.productivityapp

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PersonalManagerApplication : Application() {
    // Using by lazy so the database and repositories are only created when they're needed
    val database by lazy { AppDatabase.getDatabase(this) }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
    val expenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
    val noteRepository by lazy { NoteRepository(database.noteDao()) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    val transactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        // Initialize default categories on first launch
        applicationScope.launch {
            categoryRepository.insertAll(DefaultCategories.getAllDefaults())
        }
    }
}
