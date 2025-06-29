package com.nayab.expenses.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.nayab.expenses.model.Expense

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllExpenses(): LiveData<List<Expense>>
}
