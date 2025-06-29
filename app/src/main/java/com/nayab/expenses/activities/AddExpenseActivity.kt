package com.nayab.expenses.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nayab.smartexpensetracker.databinding.ActivityAddExpenseBinding
import com.nayab.expenses.model.Expense
import com.nayab.expenses.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var viewModel: ExpenseViewModel
    private val calendar = Calendar.getInstance()
    private val OCR_REQUEST_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ExpenseViewModel(application)

        // Spinner setup
        val categories = listOf("Food", "Transport", "Shopping", "Bills", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        // Date picker
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.etDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                binding.etDate.setText(dateFormat.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Save expense
        binding.btnSaveExpense.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val amount = binding.etAmount.text.toString().toDoubleOrNull()
            val category = binding.spinnerCategory.selectedItem.toString()
            val date = binding.etDate.text.toString()

            if (title.isEmpty() || amount == null || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                val expense = Expense(title = title, amount = amount, category = category, date = date)
                viewModel.insert(expense)
                Toast.makeText(this, "Expense saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Launch OCR scan
        binding.btnScanReceipt.setOnClickListener {
            val intent = Intent(this, OCRScanActivity::class.java)
            startActivityForResult(intent, OCR_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OCR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let {
                binding.etTitle.setText(it.getStringExtra("scanned_title") ?: "")
                binding.etAmount.setText(it.getStringExtra("scanned_amount") ?: "")
                binding.etDate.setText(it.getStringExtra("scanned_date") ?: "")
            }
        }
    }
}
