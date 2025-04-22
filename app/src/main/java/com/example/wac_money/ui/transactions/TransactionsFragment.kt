package com.example.wac_money.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wac_money.R
import com.example.wac_money.data.TransactionDatabase
import com.example.wac_money.model.Transaction
import com.example.wac_money.model.TransactionType
import com.example.wac_money.screens.AddTransactionActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class TransactionsFragment : Fragment() {
    companion object {
        private const val TAG = "TransactionsFragment"
    }

    private lateinit var transactionDatabase: TransactionDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var emptyView: TextView
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database
        transactionDatabase = TransactionDatabase(requireContext())

        // Initialize views
        recyclerView = view.findViewById(R.id.transactionsList)
        emptyView = view.findViewById(R.id.emptyView)

        // Set up RecyclerView
        setupRecyclerView()

        // Find the FAB and set click listener
        val addTransactionFab = view.findViewById<FloatingActionButton>(R.id.addTransactionFab)
        addTransactionFab.setOnClickListener {
            Log.d(TAG, "Add transaction FAB clicked")
            navigateToAddTransaction()
        }

        // Load transactions
        loadTransactions()
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(emptyList()) { transaction ->
            // Handle transaction click (edit)
            val intent = Intent(requireContext(), AddTransactionActivity::class.java)
            intent.putExtra("transaction_id", transaction.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
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

    private fun navigateToAddTransaction() {
        try {
            val intent = Intent(requireContext(), AddTransactionActivity::class.java)
            startActivity(intent)
            Log.d(TAG, "Navigated to AddTransactionActivity")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to AddTransactionActivity", e)
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
            }
        }
    }
}
