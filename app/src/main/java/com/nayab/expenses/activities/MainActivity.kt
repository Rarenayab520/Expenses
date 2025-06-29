package com.nayab.expenses.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.Description
import com.nayab.expenses.adapter.ExpenseAdapter
import com.nayab.expenses.model.Expense
import com.nayab.expenses.viewmodel.ExpenseViewModel
import com.nayab.smartexpensetracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: ExpenseAdapter
    private var expenses: List<Expense> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ExpenseAdapter(expenses) { expense ->
            viewModel.delete(expense)
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.allExpenses.observe(this) { list ->
            expenses = list
            adapter = ExpenseAdapter(expenses) { expense ->
                viewModel.delete(expense)
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
            }
            binding.recyclerView.adapter = adapter
            updatePieChart(list)
        }

        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updatePieChart(expenses: List<Expense>) {
        val categoryTotals = mutableMapOf<String, Float>()

        expenses.forEach {
            val current = categoryTotals[it.category] ?: 0f
            categoryTotals[it.category] = current + it.amount.toFloat()
        }

        if (categoryTotals.isEmpty()) {
            binding.pieChart.clear()
            binding.pieChart.setNoDataText("No data to display")
            binding.pieChart.invalidate()
            return
        }

        // 🟢 Pie entries with empty labels (so slice doesn't show label)
        val categoryNames = categoryTotals.keys.toList()
        val entries = categoryTotals.values.mapIndexed { index, value ->
            PieEntry(value, categoryNames[index]) // ✅ Keep label for legend only
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = android.graphics.Color.BLACK

        val pieData = PieData(dataSet)

        binding.pieChart.apply {
            data = pieData
            setUsePercentValues(false)
            setDrawEntryLabels(false) // ❌ Hide text label from pie slice

            // ✅ Configure legend to show category labels manually
            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                textSize = 10f
                formSize = 12f
                textColor = android.graphics.Color.WHITE
                isWordWrapEnabled = true
            }

            // ✅ Disable chart description
            description.isEnabled = false

            // ✅ Animate the chart
            animateY(1000)
            invalidate()
        }

    }

}
