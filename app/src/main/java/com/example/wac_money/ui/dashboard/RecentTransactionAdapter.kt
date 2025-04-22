package com.example.wac_money.ui.dashboard

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wac_money.R
import com.example.wac_money.data.Transaction

class RecentTransactionAdapter : ListAdapter<Transaction, RecentTransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    companion object {
        private const val TAG = "RecentTransactionAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        try {
            val transaction = getItem(position)
            Log.d(TAG, "Binding transaction: ${transaction.id} - ${transaction.title}")
            holder.bind(transaction)
        } catch (e: Exception) {
            Log.e(TAG, "Error binding transaction at position $position", e)
        }
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.transactionTitle)
        private val amountTextView: TextView = itemView.findViewById(R.id.transactionAmount)
        private val dateTextView: TextView = itemView.findViewById(R.id.transactionDate)

        fun bind(transaction: Transaction) {
            try {
                Log.d(TAG, "Binding transaction details: ${transaction.id}")

                // Set text values
                titleTextView.text = transaction.title
                amountTextView.text = transaction.getFormattedAmount()
                dateTextView.text = transaction.getFormattedDate()

                // Set amount color based on transaction type
                val colorRes = if (transaction.isIncome()) R.color.income_green else R.color.expense_red
                amountTextView.setTextColor(itemView.context.getColor(colorRes))

                Log.d(TAG, "Successfully bound transaction: ${transaction.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error binding transaction details: ${transaction.id}", e)
            }
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
