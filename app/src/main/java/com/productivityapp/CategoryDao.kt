package com.productivityapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT DISTINCT * FROM categories GROUP BY name, type ORDER BY isCustom ASC, name ASC")
    fun getAllCategories(): Flow<List<Category>>
    
    @Query("SELECT DISTINCT * FROM categories WHERE type = :type OR type = 'BOTH' GROUP BY name ORDER BY isCustom ASC, name ASC")
    fun getCategoriesByType(type: String): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>)
    
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
    
    @Update
    suspend fun update(category: Category)
    
    @Delete
    suspend fun delete(category: Category)
    
    @Query("DELETE FROM categories WHERE isCustom = 1 AND id = :id")
    suspend fun deleteCustomCategory(id: Long)
}
