package com.example.wac_money.screens

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wac_money.R
import com.example.wac_money.data.TransactionDatabase
import com.example.wac_money.model.Transaction
import com.example.wac_money.model.TransactionType
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var titleEditText: TextInputEditText
    private lateinit var amountEditText: TextInputEditText
    private lateinit var categorySpinner: TextInputLayout
    private lateinit var typeSpinner: TextInputLayout
    private lateinit var noteEditText: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var db: TransactionDatabase

    private val categories = listOf(
        "Food & Dining",
        "Transportation",
        "Shopping",
        "Entertainment",
        "Bills & Utilities",
        "Health & Fitness",
        "Education",
        "Travel",
        "Other"
    )

    private lateinit var transactionDatabase: TransactionDatabase
    private var selectedDate: Date = Date()
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    private var isEditMode = false
    private var transactionId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("AddTransactionActivity", "onCreate called")

        try {
            setContentView(R.layout.activity_add_transaction)
            android.util.Log.d("AddTransactionActivity", "setContentView completed")

            transactionDatabase = TransactionDatabase(this)
            android.util.Log.d("AddTransactionActivity", "TransactionDatabase initialized")

            // Initialize views
            try {
                titleEditText = findViewById(R.id.titleEditText)
                amountEditText = findViewById(R.id.amountEditText)
                categorySpinner = findViewById(R.id.categorySpinner)
                typeSpinner = findViewById(R.id.typeSpinner)
                noteEditText = findViewById(R.id.noteEditText)
                saveButton = findViewById(R.id.saveButton)
                android.util.Log.d("AddTransactionActivity", "Views initialized successfully")
            } catch (e: Exception) {
                android.util.Log.e("AddTransactionActivity", "Error initializing views", e)
                Toast.makeText(this, "Error initializing views: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            // Check if we're in edit mode
            transactionId = intent.getLongExtra("transaction_id", -1)
            if (transactionId != null && transactionId != -1L) {
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

    private fun loadTransaction(id: Long) {
        val transaction = transactionDatabase.getTransaction(id)
        if (transaction != null) {
            titleEditText.setText(transaction.title)
            amountEditText.setText(transaction.amount.toString())
            // Update category and type spinners
            val categoryAutoComplete = categorySpinner.editText as? AutoCompleteTextView
            categoryAutoComplete?.setText(transaction.category, false)
            val typeAutoComplete = typeSpinner.editText as? AutoCompleteTextView
            typeAutoComplete?.setText(transaction.type.name, false)
            selectedDate = transaction.date
            updateDateDisplay()
        }
    }

    private fun setupCategoryDropdown() {
        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        val categoryAutoComplete = categorySpinner.editText as? AutoCompleteTextView
        categoryAutoComplete?.setAdapter(categoryAdapter)
    }

    private fun setupDatePicker() {
        // Implementation of setupDatePicker method
    }

    private fun updateDateDisplay() {
        // Implementation of updateDateDisplay method
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            saveTransaction()
        }
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        val view = toast.view
        view?.setBackgroundResource(android.R.drawable.toast_frame)
        val text = view?.findViewById<android.widget.TextView>(android.R.id.message)
        text?.apply {
            textSize = 18f
            setTextColor(android.graphics.Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setPadding(32, 16, 32, 16)
        }
        toast.duration = Toast.LENGTH_LONG
        toast.show()
    }

    private fun saveTransaction() {
        val title = titleEditText.text.toString()
        val amountStr = amountEditText.text.toString()
        val category = (categorySpinner.editText as? AutoCompleteTextView)?.text.toString()
        val type = TransactionType.values().find { it.name == (typeSpinner.editText as? AutoCompleteTextView)?.text.toString() } ?: TransactionType.EXPENSE
        val note = noteEditText.text.toString()

        if (title.isBlank() || amountStr.isBlank()) {
            showToast("Please fill in all required fields")
            return
        }

        try {
            val amount = amountStr.toDouble()
            val transaction = if (isEditMode && transactionId != null) {
                Transaction(
                    id = transactionId!!,
                    title = title,
                    amount = amount,
                    category = category,
                    date = selectedDate,
                    type = type,
                    note = note.takeIf { it.isNotBlank() }
                )
            } else {
                Transaction(
                    title = title,
                    amount = amount,
                    category = category,
                    date = selectedDate,
                    type = type,
                    note = note.takeIf { it.isNotBlank() }
                )
            }

            val success = if (isEditMode) {
                transactionDatabase.updateTransaction(transaction) > 0
            } else {
                transactionDatabase.addTransaction(transaction) > 0
            }

            if (success) {
                showToast(if (isEditMode) "Transaction updated successfully!" else "Transaction added successfully!")
                finish()
            } else {
                showToast("Failed to ${if (isEditMode) "update" else "add"} transaction. Please try again.")
            }
        } catch (e: NumberFormatException) {
            showToast("Please enter a valid amount")
        }
    }
}
