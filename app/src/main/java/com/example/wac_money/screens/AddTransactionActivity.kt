package com.example.wac_money.screens

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wac_money.R
import com.example.wac_money.data.TransactionDatabase
import com.example.wac_money.model.Transaction
import com.example.wac_money.model.TransactionCategory
import com.example.wac_money.model.TransactionType
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var titleInput: TextInputEditText
    private lateinit var amountInput: TextInputEditText
    private lateinit var categoryDropdown: AutoCompleteTextView
    private lateinit var dateInput: TextInputEditText
    private lateinit var typeSwitch: Switch
    private lateinit var saveButton: Button

    private lateinit var transactionDatabase: TransactionDatabase
    private var selectedDate: Date = Date()
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    private var isEditMode = false
    private var transactionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("AddTransactionActivity", "onCreate called")

        try {
            setContentView(R.layout.add_transaction_screen)
            android.util.Log.d("AddTransactionActivity", "setContentView completed")

            transactionDatabase = TransactionDatabase(this)
            android.util.Log.d("AddTransactionActivity", "TransactionDatabase initialized")

            // Initialize views
            try {
                titleInput = findViewById(R.id.titleInput)
                amountInput = findViewById(R.id.amountInput)
                categoryDropdown = findViewById(R.id.categoryDropdown)
                dateInput = findViewById(R.id.dateInput)
                typeSwitch = findViewById(R.id.typeSwitch)
                saveButton = findViewById(R.id.saveButton)
                android.util.Log.d("AddTransactionActivity", "Views initialized successfully")
            } catch (e: Exception) {
                android.util.Log.e("AddTransactionActivity", "Error initializing views", e)
                Toast.makeText(this, "Error initializing views: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            // Check if we're in edit mode
            transactionId = intent.getStringExtra("transaction_id")
            if (transactionId != null) {
                isEditMode = true
                loadTransaction(transactionId!!)
            }

            // Set up category dropdown
            setupCategoryDropdown()

            // Set up date picker
            setupDatePicker()

            // Set up save button
            setupSaveButton()

            // Set initial date
            updateDateDisplay()

            android.util.Log.d("AddTransactionActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("AddTransactionActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun loadTransaction(id: String) {
        val transaction = transactionDatabase.getTransaction(id)
        if (transaction != null) {
            titleInput.setText(transaction.title)
            amountInput.setText(transaction.amount.toString())
            categoryDropdown.setText(transaction.category.name)
            selectedDate = transaction.date
            typeSwitch.isChecked = transaction.type == TransactionType.INCOME
            updateDateDisplay()
        }
    }

    private fun setupCategoryDropdown() {
        val categories = TransactionCategory.values().map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        categoryDropdown.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate

            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDate = calendar.time
                    updateDateDisplay()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateDisplay() {
        dateInput.setText(dateFormatter.format(selectedDate))
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            val title = titleInput.text.toString()
            val amountStr = amountInput.text.toString()
            val categoryStr = categoryDropdown.text.toString()

            if (title.isBlank() || amountStr.isBlank() || categoryStr.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = try {
                TransactionCategory.valueOf(categoryStr)
            } catch (e: IllegalArgumentException) {
                Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val type = if (typeSwitch.isChecked) TransactionType.INCOME else TransactionType.EXPENSE

            val transaction = if (isEditMode && transactionId != null) {
                Transaction(
                    id = transactionId!!,
                    title = title,
                    amount = amount,
                    category = category,
                    date = selectedDate,
                    type = type
                )
            } else {
                Transaction(
                    title = title,
                    amount = amount,
                    category = category,
                    date = selectedDate,
                    type = type
                )
            }

            val success = if (isEditMode) {
                transactionDatabase.updateTransaction(transaction) > 0
            } else {
                transactionDatabase.addTransaction(transaction) > 0
            }

            if (success) {
                Toast.makeText(
                    this,
                    if (isEditMode) "Transaction updated" else "Transaction added",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Failed to ${if (isEditMode) "update" else "add"} transaction",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
