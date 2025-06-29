package com.nayab.expenses.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.nayab.expenses.data.ExpenseDatabase
import com.nayab.expenses.model.Expense
import com.nayab.expenses.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    val allExpenses: LiveData<List<Expense>>

    init {
        val dao = ExpenseDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(dao)
        allExpenses = repository.allExpenses
    }

    fun insert(expense: Expense) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(expense)
    }

    fun delete(expense: Expense) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(expense)
    }
}
