package com.example.wac_money.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wac_money.R
import com.example.wac_money.data.TransactionDatabase
import com.example.wac_money.model.Transaction
import com.example.wac_money.model.TransactionType
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class TransactionListActivity : AppCompatActivity() {
    private lateinit var transactionDatabase: TransactionDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var addButton: FloatingActionButton
    private lateinit var emptyView: TextView

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_list)

        transactionDatabase = TransactionDatabase(this)

        recyclerView = findViewById(R.id.transactionRecyclerView)
        addButton = findViewById(R.id.addTransactionFab)
        emptyView = findViewById(R.id.emptyView)

        setupRecyclerView()
        setupAddButton()
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(emptyList()) { transaction ->
            // Handle transaction click (edit)
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("transaction_id", transaction.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupAddButton() {
        try {
            addButton.setOnClickListener {
                try {
                    android.util.Log.d("TransactionListActivity", "Add button clicked")
                    val intent = Intent(this, AddTransactionActivity::class.java)
                    startActivity(intent)
                    android.util.Log.d("TransactionListActivity", "AddTransactionActivity started")
                } catch (e: Exception) {
                    android.util.Log.e("TransactionListActivity", "Error starting AddTransactionActivity", e)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            android.util.Log.d("TransactionListActivity", "Add button click listener set up successfully")
        } catch (e: Exception) {
            android.util.Log.e("TransactionListActivity", "Error setting up add button", e)
            Toast.makeText(this, "Error setting up add button: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadTransactions() {
        val transactions = transactionDatabase.getAllTransactions()
        adapter.updateTransactions(transactions)

        if (transactions.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    inner class TransactionAdapter(
        private var transactions: List<Transaction>,
        private val onItemClick: (Transaction) -> Unit
    ) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

        fun updateTransactions(newTransactions: List<Transaction>) {
            transactions = newTransactions
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_transaction, parent, false)
            return TransactionViewHolder(view)
        }

        override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
            val transaction = transactions[position]
            holder.bind(transaction)
        }

        override fun getItemCount(): Int = transactions.size

        inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleTextView: TextView = itemView.findViewById(R.id.transactionTitle)
            private val amountTextView: TextView = itemView.findViewById(R.id.transactionAmount)
            private val categoryTextView: TextView = itemView.findViewById(R.id.transactionCategory)
            private val dateTextView: TextView = itemView.findViewById(R.id.transactionDate)
            private val typeTextView: TextView = itemView.findViewById(R.id.transactionType)

            fun bind(transaction: Transaction) {
                titleTextView.text = transaction.title
                amountTextView.text = String.format("$%.2f", transaction.amount)
                categoryTextView.text = transaction.category
                dateTextView.text = dateFormatter.format(transaction.date)
                typeTextView.text = transaction.type.name

                // Set color based on transaction type
                val colorRes = if (transaction.type == TransactionType.INCOME) {
                    R.color.income_green
                } else {
                    R.color.expense_red
                }
                amountTextView.setTextColor(resources.getColor(colorRes, null))

                itemView.setOnClickListener {
                    onItemClick(transaction)
                }

                itemView.setOnLongClickListener {
                    showDeleteDialog(transaction)
                    true
                }
            }

            private fun showDeleteDialog(transaction: Transaction) {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete") { _, _ ->
                        transactionDatabase.deleteTransaction(transaction.id)
                        loadTransactions()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
}
