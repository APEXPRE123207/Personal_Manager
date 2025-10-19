package com.productivityapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

    val allCategories: StateFlow<List<Category>> = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun getCategoriesByType(type: CategoryType): StateFlow<List<Category>> {
        return repository.getCategoriesByType(type).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    fun insert(category: Category) = viewModelScope.launch {
        repository.insert(category)
    }
    
    fun update(category: Category) = viewModelScope.launch {
        repository.update(category)
    }
    
    fun delete(category: Category) = viewModelScope.launch {
        repository.delete(category)
    }
    
    fun deleteCustomCategory(id: Long) = viewModelScope.launch {
        repository.deleteCustomCategory(id)
    }
    
    // Initialize default categories on first launch
    fun initializeDefaultCategories() = viewModelScope.launch {
        repository.insertAll(DefaultCategories.getAllDefaults())
    }
}

class CategoryViewModelFactory(private val repository: CategoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
