package com.example.wac_money.screens

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wac_money.R
import com.example.wac_money.data.Transaction
import com.example.wac_money.data.TransactionDatabase
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "AddTransactionActivity"
        const val EXTRA_TRANSACTION_ID = "transaction_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_CATEGORY = "category"
        const val EXTRA_TYPE = "type"
        const val EXTRA_DATE = "date"
        const val EXTRA_NOTE = "note"
    }

    private lateinit var titleEditText: TextInputEditText
    private lateinit var amountEditText: TextInputEditText
    private lateinit var categorySpinner: TextInputLayout
    private lateinit var typeSpinner: TextInputLayout
    private lateinit var noteEditText: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var dateButton: MaterialButton
    private lateinit var transactionDatabase: TransactionDatabase

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

    private val types = listOf("income", "expense")

    private var selectedDate: Date = Date()
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    private var isEditMode = false
    private var transactionId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        try {
            setContentView(R.layout.activity_add_transaction)
            Log.d(TAG, "setContentView completed")

            transactionDatabase = TransactionDatabase(this)
            Log.d(TAG, "TransactionDatabase initialized")

            // Initialize views
            try {
                titleEditText = findViewById(R.id.titleEditText)
                amountEditText = findViewById(R.id.amountEditText)
                categorySpinner = findViewById(R.id.categorySpinner)
                typeSpinner = findViewById(R.id.typeSpinner)
                noteEditText = findViewById(R.id.noteEditText)
                saveButton = findViewById(R.id.saveButton)
                dateButton = findViewById(R.id.dateButton)
                Log.d(TAG, "Views initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing views", e)
                Toast.makeText(this, "Error initializing views: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            // Check if we're in edit mode
            transactionId = intent.getLongExtra(EXTRA_TRANSACTION_ID, -1)
            if (transactionId != null && transactionId != -1L) {
                isEditMode = true
                loadTransaction(transactionId!!)
            }

            // Set up category dropdown
            setupCategoryDropdown()

            // Set up type dropdown
            setupTypeDropdown()

            // Set up date picker
            setupDatePicker()

            // Set up save button
            setupSaveButton()

            // Set initial date
            updateDateDisplay()

            Log.d(TAG, "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun loadTransaction(id: Long) {
        try {
            Log.d(TAG, "Loading transaction with ID: $id")
        val transaction = transactionDatabase.getTransaction(id)
        if (transaction != null) {
                titleEditText.setText(transaction.title)
                amountEditText.setText(transaction.amount.toString())
                // Update category and type spinners
                val categoryAutoComplete = categorySpinner.editText as? AutoCompleteTextView
                categoryAutoComplete?.setText(transaction.category, false)
                val typeAutoComplete = typeSpinner.editText as? AutoCompleteTextView
                typeAutoComplete?.setText(transaction.type, false)
            selectedDate = transaction.date
            updateDateDisplay()
                noteEditText.setText(transaction.note)
                Log.d(TAG, "Transaction loaded successfully")
            } else {
                Log.e(TAG, "Transaction not found with ID: $id")
                Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading transaction", e)
            Toast.makeText(this, "Error loading transaction: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupCategoryDropdown() {
        try {
            val categoryAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
            )
            val categoryAutoComplete = categorySpinner.editText as? AutoCompleteTextView
            categoryAutoComplete?.setAdapter(categoryAdapter)
            Log.d(TAG, "Category dropdown setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up category dropdown", e)
            Toast.makeText(this, "Error setting up category dropdown: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupTypeDropdown() {
        try {
            val typeAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                types
            )
            val typeAutoComplete = typeSpinner.editText as? AutoCompleteTextView
            typeAutoComplete?.setAdapter(typeAdapter)

            // Set default value to expense
            typeAutoComplete?.setText(types[1], false)
            Log.d(TAG, "Type dropdown setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up type dropdown", e)
            Toast.makeText(this, "Error setting up type dropdown: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupDatePicker() {
        try {
            dateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate

                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        calendar.set(selectedYear, selectedMonth, selectedDay)
                    selectedDate = calendar.time
                    updateDateDisplay()
                },
                    year,
                    month,
                    day
            ).show()
            }
            Log.d(TAG, "Date picker setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up date picker", e)
            Toast.makeText(this, "Error setting up date picker: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateDateDisplay() {
        dateButton.text = dateFormatter.format(selectedDate)
    }

    private fun setupSaveButton() {
        try {
        saveButton.setOnClickListener {
                saveTransaction()
            }
            Log.d(TAG, "Save button setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up save button", e)
            Toast.makeText(this, "Error setting up save button: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveTransaction() {
        try {
            val title = titleEditText.text.toString()
            val amountStr = amountEditText.text.toString()
            val category = (categorySpinner.editText as? AutoCompleteTextView)?.text.toString()
            val type = (typeSpinner.editText as? AutoCompleteTextView)?.text.toString().lowercase()
            val note = noteEditText.text.toString()

            Log.d(TAG, "Saving transaction: title=$title, amount=$amountStr, category=$category, type=$type")

            if (title.isBlank() || amountStr.isBlank() || category.isBlank() || type.isBlank()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return
            }

            val transaction = Transaction(
                id = transactionId ?: 0,
                    title = title,
                    amount = amount,
                    category = category,
                type = type,
                    date = selectedDate,
                note = note
            )

            if (isEditMode) {
                transactionDatabase.updateTransaction(transaction)
                Toast.makeText(this, "Transaction updated successfully", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Transaction updated with ID: ${transaction.id}")
            } else {
                val id = transactionDatabase.addTransaction(transaction)
                Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Transaction added with ID: $id")
            }

                finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving transaction", e)
            Toast.makeText(this, "Error saving transaction: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
