package com.productivityapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    private val _selectedDateRange = MutableStateFlow(DateRange.THIS_MONTH)
    val selectedDateRange: StateFlow<DateRange> = _selectedDateRange.asStateFlow()
    
    private val _selectedType = MutableStateFlow<TransactionType?>(null)
    val selectedType: StateFlow<TransactionType?> = _selectedType.asStateFlow()
    
    // Filtered transactions based on selected filters
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        allTransactions,
        selectedDateRange,
        selectedType
    ) { transactions, dateRange, type ->
        val (start, end) = dateRange.getDates()
        transactions.filter { transaction ->
            val dateMatches = transaction.date >= start && transaction.date <= end
            val typeMatches = type == null || transaction.type == type
            dateMatches && typeMatches
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Financial statistics
    val financialStats: StateFlow<FinancialStats> = filteredTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    ).combine(selectedDateRange) { transactions, _ ->
        FinancialStats(
            totalIncome = transactions.totalIncome(),
            totalExpenses = transactions.totalExpenses(),
            balance = transactions.balance(),
            transactionCount = transactions.size,
            incomeCount = transactions.income().size,
            expenseCount = transactions.expenses().size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialStats()
    )
    
    fun setDateRange(range: DateRange) {
        _selectedDateRange.value = range
    }
    
    fun setTransactionType(type: TransactionType?) {
        _selectedType.value = type
    }
    
    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }
    
    fun update(transaction: Transaction) = viewModelScope.launch {
        repository.update(transaction)
    }
    
    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }
    
    suspend fun getTotalIncome(startDate: Date, endDate: Date): Double {
        return repository.getTotalIncome(startDate, endDate)
    }
    
    suspend fun getTotalExpenses(startDate: Date, endDate: Date): Double {
        return repository.getTotalExpenses(startDate, endDate)
    }
}

class TransactionViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class FinancialStats(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val balance: Double = 0.0,
    val transactionCount: Int = 0,
    val incomeCount: Int = 0,
    val expenseCount: Int = 0
) {
    val savingsRate: Double
        get() = if (totalIncome > 0) (balance / totalIncome) * 100 else 0.0
}

enum class DateRange {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR,
    CUSTOM;
    
    fun getDates(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val end = calendar.time
        
        calendar.apply {
            when (this@DateRange) {
                TODAY -> set(Calendar.HOUR_OF_DAY, 0)
                THIS_WEEK -> {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(Calendar.HOUR_OF_DAY, 0)
                }
                THIS_MONTH -> {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                }
                THIS_YEAR -> {
                    set(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                }
                CUSTOM -> return Pair(Date(0), Date())
            }
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        return Pair(calendar.time, end)
    }
}
