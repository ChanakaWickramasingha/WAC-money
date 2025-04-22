package com.example.wac_money.data

import android.content.Context
import android.util.Log

class DashboardRepository(private val context: Context) {
    companion object {
        private const val TAG = "DashboardRepository"
    }

    private val transactionDatabase = TransactionDatabase(context)

    /**
     * Get all transactions from the database
     */
    fun getAllTransactions(): List<Transaction> {
        try {
            return transactionDatabase.getAllTransactions()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all transactions", e)
            return emptyList()
        }
    }

    /**
     * Calculate total income from all income transactions
     */
    fun calculateTotalIncome(): Double {
        try {
            return getAllTransactions()
                .filter { it.isIncome() }
                .sumOf { it.amount }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating total income", e)
            return 0.0
        }
    }

    /**
     * Calculate total expenses from all expense transactions
     */
    fun calculateTotalExpenses(): Double {
        try {
            return getAllTransactions()
                .filter { it.isExpense() }
                .sumOf { it.amount }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating total expenses", e)
            return 0.0
        }
    }

    /**
     * Calculate total balance (income - expenses)
     */
    fun calculateTotalBalance(): Double {
        try {
            return calculateTotalIncome() - calculateTotalExpenses()
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating total balance", e)
            return 0.0
        }
    }

    /**
     * Get recent transactions (last 5)
     */
    fun getRecentTransactions(limit: Int = 5): List<Transaction> {
        try {
            return getAllTransactions().take(limit)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent transactions", e)
            return emptyList()
        }
    }
}
