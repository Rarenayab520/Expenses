package com.nayab.expenses.repository
import com.nayab.expenses.data.ExpenseDao
import com.nayab.expenses.model.Expense

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    val allExpenses = expenseDao.getAllExpenses()

    suspend fun insert(expense: Expense) {
        expenseDao.insert(expense)
    }

    suspend fun delete(expense: Expense) {
        expenseDao.delete(expense)
    }
}
