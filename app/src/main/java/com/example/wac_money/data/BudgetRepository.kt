package com.example.wac_money.data

import android.content.Context
import android.util.Log
import java.util.Calendar

class BudgetRepository(private val context: Context) {
    companion object {
        private const val TAG = "BudgetRepository"
        private const val WARNING_THRESHOLD = 0.8 // 80% of budget
    }

    private val budgetDatabase = BudgetDatabase(context)
    private val transactionDatabase = TransactionDatabase(context)

    fun saveBudget(amount: Double): Budget {
        try {
            val calendar = Calendar.getInstance()
            val budget = Budget(
                amount = amount,
                month = calendar.get(Calendar.MONTH) + 1,
                year = calendar.get(Calendar.YEAR)
            )
            val id = budgetDatabase.saveBudget(budget)
            Log.d(TAG, "Budget saved with id: $id")
            return budget.copy(id = id)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving budget", e)
            throw e
        }
    }

    fun getCurrentBudget(): Budget? {
        try {
            return budgetDatabase.getCurrentBudget()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current budget", e)
            return null
        }
    }

    fun getCurrentSpending(): Double {
        try {
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH) + 1
            val currentYear = calendar.get(Calendar.YEAR)

            return transactionDatabase.getAllTransactions()
                .filter { transaction ->
                    val transactionDate = Calendar.getInstance().apply {
                        time = transaction.date
                    }
                    transaction.isExpense() &&
                    transactionDate.get(Calendar.MONTH) + 1 == currentMonth &&
                    transactionDate.get(Calendar.YEAR) == currentYear
                }
                .sumOf { it.amount }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current spending", e)
            return 0.0
        }
    }

    fun getBudgetProgress(): BudgetProgress {
        try {
            val budget = getCurrentBudget()
            val spending = getCurrentSpending()

            return if (budget != null) {
                val progress = (spending / budget.amount).coerceIn(0.0, 1.0)
                val isWarning = progress >= WARNING_THRESHOLD
                val isExceeded = progress >= 1.0

                BudgetProgress(
                    budget = budget,
                    spending = spending,
                    progress = progress,
                    isWarning = isWarning,
                    isExceeded = isExceeded
                )
            } else {
                BudgetProgress(
                    budget = null,
                    spending = spending,
                    progress = 0.0,
                    isWarning = false,
                    isExceeded = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting budget progress", e)
            return BudgetProgress(
                budget = null,
                spending = 0.0,
                progress = 0.0,
                isWarning = false,
                isExceeded = false
            )
        }
    }
}

data class BudgetProgress(
    val budget: Budget?,
    val spending: Double,
    val progress: Double,
    val isWarning: Boolean,
    val isExceeded: Boolean
)
