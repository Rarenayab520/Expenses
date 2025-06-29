package com.nayab.expenses.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nayab.smartexpensetracker.R
import com.nayab.smartexpensetracker.databinding.ItemExpenseBinding
import com.nayab.expenses.model.Expense

class ExpenseAdapter(
    private val expenseList: List<Expense>,
    private val onItemLongClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.binding.tvTitle.text = expense.title
        holder.binding.tvAmount.text = "Rs. ${expense.amount}"
        holder.binding.tvCategory.text = expense.category
        holder.binding.tvDate.text = expense.date

        // Long click to delete
        holder.itemView.setOnLongClickListener {
            onItemLongClick(expense)
            true
        }
    }

    override fun getItemCount(): Int = expenseList.size
}
