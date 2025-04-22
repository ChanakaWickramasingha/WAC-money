package com.example.wac_money.ui.budget

import android.content.Context
import android.util.AttributeSet
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

    private val progressIndicator: CircularProgressIndicator
    private val progressText: TextView
    private val spentAmount: TextView
    private val budgetAmount: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_budget_gauge, this, true)

        progressIndicator = findViewById(R.id.progressIndicator)
        progressText = findViewById(R.id.progressText)
        spentAmount = findViewById(R.id.spentAmount)
        budgetAmount = findViewById(R.id.budgetAmount)
    }

    fun updateProgress(progress: BudgetProgress) {
        val percentage = (progress.progress * 100).toInt()
        progressIndicator.progress = percentage
        progressText.text = "$percentage%"

        spentAmount.text = "Spent: ${formatCurrency(progress.spending)}"
        budgetAmount.text = "Budget: ${formatCurrency(progress.budget?.amount ?: 0.0)}"

        // Update colors based on progress
        val colorRes = when {
            progress.isExceeded -> R.color.expense_red
            progress.isWarning -> R.color.warning_yellow
            else -> R.color.primary
        }
        progressIndicator.setIndicatorColor(context.getColor(colorRes))
    }

    private fun formatCurrency(amount: Double): String {
        return java.text.NumberFormat.getCurrencyInstance().format(amount)
    }
}
