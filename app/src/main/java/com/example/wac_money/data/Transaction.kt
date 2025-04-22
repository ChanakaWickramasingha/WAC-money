package com.example.wac_money.data

import java.util.Date

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val type: String,
    val date: Date,
    val note: String = ""
) {
    init {
        require(title.isNotBlank()) { "Title cannot be empty" }
        require(amount > 0) { "Amount must be greater than 0" }
        require(category.isNotBlank()) { "Category cannot be empty" }
        require(type in listOf("income", "expense")) { "Type must be either 'income' or 'expense'" }
    }

    fun isIncome(): Boolean = type == "income"

    fun isExpense(): Boolean = type == "expense"

    fun getFormattedAmount(): String {
        val prefix = if (isIncome()) "+" else "-"
        return "$prefix$${String.format("%.2f", amount)}"
    }

    fun getFormattedDate(): String {
        return java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            .format(date)
    }

    companion object {
        fun createIncome(
            title: String,
            amount: Double,
            category: String,
            date: Date = Date(),
            note: String = ""
        ): Transaction = Transaction(
            title = title,
            amount = amount,
            category = category,
            type = "income",
            date = date,
            note = note
        )

        fun createExpense(
            title: String,
            amount: Double,
            category: String,
            date: Date = Date(),
            note: String = ""
        ): Transaction = Transaction(
            title = title,
            amount = amount,
            category = category,
            type = "expense",
            date = date,
            note = note
        )
    }
}
