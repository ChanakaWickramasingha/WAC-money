package com.example.wac_money.ui.budget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.wac_money.R
import com.example.wac_money.data.BudgetProgress
import com.google.android.material.progressindicator.CircularProgressIndicator

class BudgetGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "BudgetGaugeView"
    }

    private val progressIndicator: CircularProgressIndicator
    private val progressText: TextView
    private val spentAmount: TextView
    private val budgetAmount: TextView
    private val remainingAmount: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_budget_gauge, this, true)

        progressIndicator = findViewById(R.id.progressIndicator)
        progressText = findViewById(R.id.progressText)
        spentAmount = findViewById(R.id.spentAmount)
        budgetAmount = findViewById(R.id.budgetAmount)
        remainingAmount = findViewById(R.id.remainingAmount)

        Log.d(TAG, "BudgetGaugeView initialized")
    }

    fun updateProgress(progress: BudgetProgress) {
        try {
            Log.d(TAG, "Updating progress: budget=${progress.budget?.amount}, spending=${progress.spending}, progress=${progress.progress}")

            val percentage = (progress.progress * 100).toInt()
            progressIndicator.progress = percentage
            progressText.text = "$percentage%"

            val budgetAmountValue = progress.budget?.amount ?: 0.0
            val remaining = (budgetAmountValue - progress.spending).coerceAtLeast(0.0)

            spentAmount.text = "Spent: ${formatCurrency(progress.spending)}"
            budgetAmount.text = "Budget: ${formatCurrency(budgetAmountValue)}"
            remainingAmount.text = "Remaining: ${formatCurrency(remaining)}"

            // Update colors based on progress
            val colorRes = when {
                progress.isExceeded -> R.color.expense_red
                progress.isWarning -> R.color.warning_yellow
                else -> R.color.primary
            }
            progressIndicator.setIndicatorColor(context.getColor(colorRes))

            Log.d(TAG, "Progress updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating progress", e)
        }
    }

    private fun formatCurrency(amount: Double): String {
        return java.text.NumberFormat.getCurrencyInstance().format(amount)
    }
}
