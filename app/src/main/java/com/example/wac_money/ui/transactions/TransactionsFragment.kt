package com.example.wac_money.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wac_money.R
import com.example.wac_money.data.Transaction
import com.example.wac_money.data.TransactionDatabase
import com.example.wac_money.screens.AddTransactionActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TransactionsFragment : Fragment() {
    companion object {
        private const val TAG = "TransactionsFragment"
    }

    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var transactionDatabase: TransactionDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        try {
            transactionDatabase = TransactionDatabase(requireContext())
            Log.d(TAG, "TransactionDatabase initialized")

            recyclerView = view.findViewById(R.id.transactionsList)
            emptyView = view.findViewById(R.id.emptyView)
            Log.d(TAG, "Views initialized")

            setupRecyclerView()
            setupFab(view)
            loadTransactions()
            Log.d(TAG, "Setup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        try {
            transactionAdapter = TransactionAdapter(
                onUpdateClick = { transaction -> handleUpdateClick(transaction) },
                onDeleteClick = { transaction -> handleDeleteClick(transaction) }
            )

            recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = transactionAdapter
            }
            Log.d(TAG, "RecyclerView setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
            Toast.makeText(requireContext(), "Error setting up list: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupFab(view: View) {
        try {
            view.findViewById<FloatingActionButton>(R.id.addTransactionFab).setOnClickListener {
                startActivity(Intent(context, AddTransactionActivity::class.java))
            }
            Log.d(TAG, "FAB setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up FAB", e)
            Toast.makeText(requireContext(), "Error setting up add button: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadTransactions() {
        try {
            Log.d(TAG, "Starting to load transactions")
            val transactions = transactionDatabase.getAllTransactions()
            Log.d(TAG, "Retrieved ${transactions.size} transactions from database")

            if (transactions.isEmpty()) {
                Log.d(TAG, "No transactions found, showing empty view")
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                Log.d(TAG, "Submitting ${transactions.size} transactions to adapter")
                transactionAdapter.submitList(transactions)
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading transactions", e)
            Toast.makeText(requireContext(), "Error loading transactions: ${e.message}", Toast.LENGTH_LONG).show()
            // Show empty view on error
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }

    private fun handleUpdateClick(transaction: Transaction) {
        try {
            val intent = Intent(context, AddTransactionActivity::class.java).apply {
                putExtra(AddTransactionActivity.EXTRA_TRANSACTION_ID, transaction.id)
                putExtra(AddTransactionActivity.EXTRA_TITLE, transaction.title)
                putExtra(AddTransactionActivity.EXTRA_AMOUNT, transaction.amount)
                putExtra(AddTransactionActivity.EXTRA_CATEGORY, transaction.category)
                putExtra(AddTransactionActivity.EXTRA_TYPE, transaction.type)
                putExtra(AddTransactionActivity.EXTRA_DATE, transaction.date.time)
                putExtra(AddTransactionActivity.EXTRA_NOTE, transaction.note)
            }
            startActivity(intent)
            Log.d(TAG, "Started update for transaction ${transaction.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error handling update click", e)
            Toast.makeText(requireContext(), "Error updating transaction: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleDeleteClick(transaction: Transaction) {
        try {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_transaction)
                .setMessage(R.string.delete_transaction_confirmation)
                .setPositiveButton(R.string.delete) { _, _ ->
                    transactionDatabase.deleteTransaction(transaction.id)
                    loadTransactions()
                    Log.d(TAG, "Deleted transaction ${transaction.id}")
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling delete click", e)
            Toast.makeText(requireContext(), "Error deleting transaction: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        loadTransactions()
    }
}
