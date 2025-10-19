package com.productivityapp

import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    
    fun getCategoriesByType(type: CategoryType): Flow<List<Category>> = 
        categoryDao.getCategoriesByType(type.name)
    
    suspend fun getCategoryById(id: Long): Category? = 
        categoryDao.getCategoryById(id)
    
    suspend fun insert(category: Category): Long {
        return categoryDao.insert(category)
    }
    
    suspend fun insertAll(categories: List<Category>) {
        // Only insert if database is empty (first time initialization)
        if (categoryDao.getCategoryCount() == 0) {
            categoryDao.insertAll(categories)
        }
    }
    
    suspend fun update(category: Category) {
        categoryDao.update(category)
    }
    
    suspend fun delete(category: Category) {
        categoryDao.delete(category)
    }
    
    suspend fun deleteCustomCategory(id: Long) {
        categoryDao.deleteCustomCategory(id)
    }
}
