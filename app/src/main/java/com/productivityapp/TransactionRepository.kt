package com.productivityapp

import kotlinx.coroutines.flow.Flow
import java.util.Date

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> = 
        transactionDao.getTransactionsByType(type.name)
    
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>> = 
        transactionDao.getTransactionsByCategory(categoryId)
    
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>> = 
        transactionDao.getTransactionsByDateRange(startDate, endDate)
    
    fun getTransactionsByTypeAndDateRange(
        type: TransactionType,
        startDate: Date,
        endDate: Date
    ): Flow<List<Transaction>> = 
        transactionDao.getTransactionsByTypeAndDateRange(type.name, startDate, endDate)
    
    suspend fun getTotalIncome(startDate: Date, endDate: Date): Double = 
        transactionDao.getTotalIncome(startDate, endDate) ?: 0.0
    
    suspend fun getTotalExpenses(startDate: Date, endDate: Date): Double = 
        transactionDao.getTotalExpenses(startDate, endDate) ?: 0.0
    
    suspend fun getExpensesByCategory(startDate: Date, endDate: Date): Map<Long?, Double> = 
        transactionDao.getExpensesByCategory(startDate, endDate)
            .associate { it.categoryId to it.total }
    
    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }
    
    suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction)
    }
    
    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }
}
